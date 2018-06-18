/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.configuration.config.lang.parser;

import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ConfigurationModule;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ExecutionContext;
import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.config.lang.parser.elements.ArrayImpl;
import com.alesharik.webserver.configuration.config.lang.parser.elements.CodeImpl;
import com.alesharik.webserver.configuration.config.lang.parser.elements.FunctionImpl;
import com.alesharik.webserver.configuration.config.lang.parser.elements.ObjectImpl;
import com.alesharik.webserver.configuration.config.lang.parser.elements.PrimitiveImpl;
import com.alesharik.webserver.configuration.config.lang.parser.elements.TypedObjectImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigurationParser {
    protected static final Pattern COMMENT_PATTERN = Pattern.compile("//.*");
    protected static final Pattern USE_REGEX = Pattern.compile("use '(?<use>.*?)'| language '(?<lang>.*?)'| context '(?<ctx>.*?)'");
    protected static final Pattern INCLUDE_REGEX = Pattern.compile("include '(?<inc>.*?)'");
    protected static final Pattern ENDPOINT_USE_REGEX = Pattern.compile("use\\s+(?<parent>.*?)\\s+as\\s+(?<name>.+)");
    protected static final Predicate<String> CLOSE_BRACKET_REGEX_PREDICATE = Pattern.compile("\\s*}\\s*").asPredicate();
    protected final Path folder;
    protected final File endpoint;
    protected final FileReader fileReader;

    public ConfigurationParser(@Nonnull File endpoint, @Nonnull FileReader fileReader) {
        this.fileReader = fileReader;
        if(!fileReader.isFile(endpoint))
            throw new IllegalArgumentException("Endpoint must be a file!");
        this.endpoint = endpoint;
        this.folder = endpoint.getParentFile().toPath();
    }

    private static String buildDefines(String line, Map<Pattern, String> defines) {
        line = COMMENT_PATTERN.matcher(line).replaceAll("").replaceAll("\\t", "    ");
        for(Map.Entry<Pattern, String> patternStringEntry : defines.entrySet()) {
            Matcher matcher = patternStringEntry.getKey().matcher(line);
            line = matcher.replaceAll(patternStringEntry.getValue());
        }
        return line;
    }

    private static String cutLine(Iterator<String> iterator) {
        String ret = iterator.next();
        iterator.remove();
        return ret;
    }

    public ConfigurationEndpoint parse() {
        List<String> endpoint = fileReader.readFile(this.endpoint.toPath());
        List<String> endpointClone = new ArrayList<>(endpoint);

        if(endpoint.isEmpty())
            throw new CodeParsingException("Endpoint file is empty!", 0, endpoint);

        List<String> headerLines;
        if(!endpoint.get(0).replaceFirst("^\\s*", "").startsWith("#"))
            headerLines = new ArrayList<>();
        else
            headerLines = cutHeader(endpoint.iterator(), StringUtils::isWhitespace, true);
        ConfigParserInstructionsHeader header = ConfigParserInstructionsHeader.parse(headerLines, null, fileReader);

        List<String> includeLines = cutHeader(endpoint.iterator(), s -> s.startsWith("endpoint"), false);
        List<ExternalLanguageHelper> helpers = extractHelpers(includeLines.iterator(), folder, endpointClone, headerLines.size());
        List<File> moduleFiles = extractModules(includeLines.iterator(), folder, endpointClone, headerLines.size());

        Map<String, ConfigurationModuleImpl> modules = moduleFiles.stream()
                .map(file -> parseModule(file, header))
                .collect(Collectors.toMap(ConfigurationModule::getName, o -> o));

        modules.values()
                .stream()
                .map(configurationModule -> configurationModule.defines)
                .reduce((header1, header2) -> {
                    header1.append(header2);
                    return header1;
                })
                .ifPresent(header::append);

        Map<Pattern, String> preparedDefines = new HashMap<>();
        header.getAllDefines().forEach((s, s2) -> preparedDefines.put(Pattern.compile(s), s2));

        String firstLine = buildDefines(cutLine(endpoint.iterator()), preparedDefines);
        int actualCodeStartLine = headerLines.size() + includeLines.size();
        String endpointName;
        {

            int off = firstLine.indexOf("endpoint ") + "endpoint ".length();
            int cutOff = firstLine.lastIndexOf(" {");

            if(off == -1)
                throw new CodeParsingException("Unexpected symbol: endpoint expected", actualCodeStartLine + 1, endpointClone);
            if(cutOff == -1)
                throw new CodeParsingException("Unexpected symbol: { expected", actualCodeStartLine, endpointClone);
            endpointName = firstLine.substring(off, cutOff).replace(" ", "");
        }

        AtomicInteger lineNumber = new AtomicInteger(actualCodeStartLine + 1);

        ApiEndpointSection apiEndpointSection = null;
        ScriptEndpointSection scriptEndpointSection = null;
        Map<String, CustomEndpointSection> sections = new HashMap<>();

        boolean in = true;
        while(!endpoint.isEmpty() && in) {
            String line = buildDefines(cutLine(endpoint.iterator()), preparedDefines);
            lineNumber.incrementAndGet();
            if(StringUtils.isWhitespace(line))
                continue;
            line = line.replaceFirst("^\\s*", "");
            if(line.startsWith("}")) {
                if(!StringUtils.isWhitespace(line.substring(1)))
                    throw new CodeParsingException("endpoint parse error: symbols after }", lineNumber.get(), endpointClone);
            } else if(line.startsWith("api "))
                apiEndpointSection = parseApiSection(line.replaceFirst("api ", ""), endpoint.iterator(), lineNumber, endpointClone, preparedDefines);
            else if(line.startsWith("script"))
                scriptEndpointSection = parseScript(line.replaceFirst("script ", ""), endpoint.iterator(), lineNumber, endpointClone, preparedDefines);
            else {
                String[] parts = line.split(" ", 2);
                String name = parts[0];
                sections.put(name, parseCustomSection(parts.length == 1 ? "" : parts[1], endpoint.iterator(), lineNumber, endpointClone, preparedDefines, modules));
            }
        }

        if(apiEndpointSection == null)
            throw new CodeParsingException("API configuration not found!", lineNumber.get(), endpointClone);
        if(scriptEndpointSection == null)
            throw new CodeParsingException("Script configuration not found!", lineNumber.get(), endpointClone);

        if(!endpoint.isEmpty()) {
            for(String line : endpoint) {
                if(!StringUtils.isWhitespace(line))
                    throw new CodeParsingException("Can't recognize symbols outside of the endpoint!", lineNumber.get(), endpointClone);
            }
        }

        return new ConfigurationEndpointImpl(endpointName, apiEndpointSection, scriptEndpointSection, helpers, new ArrayList<>(modules.values()), sections);
    }

    private ApiEndpointSection parseApiSection(String startLine, Iterator<String> lines, AtomicInteger lineNumber, List<String> linesCopy, Map<Pattern, String> preparedDefines) {
        ConfigElement element = parseElement(startLine, lines, linesCopy, lineNumber, preparedDefines, "api");
        ConfigurationElement elem = element.element;
        if(!(elem instanceof ConfigurationObject))
            throw new CodeParsingException("Api section must be an object!", lineNumber.get(), linesCopy);
        return new ApiEndpointSectionImpl((ConfigurationObject) elem);
    }

    private ScriptEndpointSection parseScript(String startLine, Iterator<String> lines, AtomicInteger lineNumber, List<String> linesCopy, Map<Pattern, String> preparedDefines) {
        Map<String, ScriptEndpointSection.ScriptSection> sections = new HashMap<>();

        //Find start
        if(!startLine.replaceFirst("^\\s*", "").startsWith("{"))
            throw new CodeParsingException("endpoint parsing error: { expected!", lineNumber.get(), linesCopy);
        if(!StringUtils.isWhitespace(startLine.replaceFirst("^\\s*", "").substring(1)))
            throw new CodeParsingException("endpoint parsing error: unexpected symbols after { !", lineNumber.get(), linesCopy);

        ScriptSectionImpl currentSection = null;
        String currentSectionName = "";
        int level = 1;//0 - end, 1 - in scripts, 2 - in section
        while(level > 0) {
            if(!lines.hasNext())
                throw new CodeParsingException("script section parsing error: } not found!", lineNumber.get(), linesCopy);
            String current = buildDefines(cutLine(lines), preparedDefines);
            lineNumber.incrementAndGet();

            if(level > 2)
                throw new CodeParsingException("script section parsing error: too many enclosures - " + level, lineNumber.get(), linesCopy);
            if(StringUtils.isWhitespace(current))
                continue;

            current = current.replaceFirst("^\\s*", "");
            if(current.startsWith("}")) {
                level--;
                if(level == 1) {//Section closed
                    sections.put(currentSectionName, currentSection);
                    currentSection = null;
                    currentSectionName = "";
                } else if(level == 0) //Script section ended
                    break;
            } else {
                if(level == 2) {//Is in section
                    String[] parts = current.split(" ", 2);
                    String name = parts[0];
                    ConfigElement element = parts.length == 1 ? null : parseElement(parts[1], lines, linesCopy, lineNumber, preparedDefines, "script", true);
                    currentSection.commands.add(new CommandImpl(name, element == null ? null : element.element));
                } else if(level == 1) {//Section start
                    String[] parts = current.split(" ", 2);
                    String name = parts[0];
                    if(parts.length > 1) {
                        String def = parts[1].replaceFirst("^\\s*", "");
                        if(def.matches("\\{\\s*}")) {//Open and close brackets found
                            sections.put(name, new ScriptSectionImpl());
                        } else if(def.startsWith("{")) {//Open bracket found
                            if(!StringUtils.isWhitespace(def.substring(1)))
                                throw new CodeParsingException("script section parsing error: unexpected symbols after { in script section!", lineNumber.get(), linesCopy);

                            currentSection = new ScriptSectionImpl();
                            currentSectionName = name;
                            level++;
                        } else {
                            while(true) {
                                String line = buildDefines(cutLine(lines), preparedDefines);
                                lineNumber.incrementAndGet();
                                if(line.replaceFirst("^\\s*", "").startsWith("{")) {
                                    currentSection = new ScriptSectionImpl();
                                    currentSectionName = name;
                                    level++;
                                    break;//Bracket found!
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ScriptEndpointSectionImpl(sections);
    }

    private CustomEndpointSection parseCustomSection(String line, Iterator<String> lines, AtomicInteger lineNumber, List<String> linesCopy, Map<Pattern, String> preparedDefines, Map<String, ConfigurationModuleImpl> modules) {
        CustomEndpointSectionImpl section = new CustomEndpointSectionImpl();

        line = line.replaceFirst("^\\s*", "");
        if(line.startsWith("{")) {
            if(!StringUtils.isWhitespace(line.substring(1)))
                throw new CodeParsingException("custom section parse error: unexpected symbols after {", lineNumber.get(), linesCopy);
        } else {
            String l;
            do {
                l = buildDefines(cutLine(lines), preparedDefines).replaceFirst("^\\s*", "");
                lineNumber.incrementAndGet();
            } while(!l.startsWith("{"));
            if(!StringUtils.isWhitespace(l.substring(1)))
                throw new CodeParsingException("custom section parse error: unexpected symbols after {", lineNumber.get(), linesCopy);
        }

        boolean in = true;
        while(in) {
            String l = buildDefines(cutLine(lines), preparedDefines).replaceFirst("^\\s*", "");
            lineNumber.incrementAndGet();
            if(StringUtils.isWhitespace(l))
                continue;
            if(l.startsWith("}")) {
                in = false;
                if(!StringUtils.isWhitespace(l.substring(1)))
                    throw new CodeParsingException("custom section parse error: unexpected symbols after }", lineNumber.get(), linesCopy);
            } else if(l.startsWith("use")) {
                Matcher matcher = ENDPOINT_USE_REGEX.matcher(l);

                String parent = "";
                String name = "";

                boolean hasConfig = false;
                while(matcher.find()) {
                    if(matcher.group("parent") != null)
                        parent = matcher.group("parent");
                    if(matcher.group("name") != null) {
                        String[] parts = matcher.group("name").split(" ", 2);
                        if(parts.length == 1 && StringUtils.isWhitespace(parts[0]))
                            throw new CodeParsingException("custom section parse error: use syntax error (empty name)", lineNumber.get(), linesCopy);
                        else if(parts.length > 1) {
                            String s = parts[1].replaceFirst("^\\s*", "");
                            if(s.startsWith("{")) {
                                hasConfig = true;
                                if(!StringUtils.isWhitespace(s.substring(1)))
                                    throw new CodeParsingException("custom section parse error: unexpected symbols after {", lineNumber.get(), linesCopy);
                            } else
                                throw new CodeParsingException("custom section parse error: unexpected symbols after use directive", lineNumber.get(), linesCopy);
                            name = parts[0];
                        } else //parts.length == 1 && parts[0] is a real name
                            name = parts[0];
                    }
                }

                if(parent.isEmpty() || name.isEmpty())
                    throw new CodeParsingException("custom section parse error: use syntax error (empty name or parent)", lineNumber.get(), linesCopy);

                ConfigurationTypedObject parentObj = null;
                if(!"null".equals(parent)) {
                    String[] parts = parent.split("\\.", 2);
                    if(parts.length < 2 || StringUtils.isWhitespace(parts[0]) || StringUtils.isWhitespace(parts[1]))
                        throw new CodeParsingException("use directive parse error: parent must follow pattern 'module_name:object_name' or be 'null'", lineNumber.get(), linesCopy);
                    String moduleName = parts[0];
                    String objectName = parts[1];
                    ConfigurationModule module = findModule(moduleName, modules.values());
                    if(module == null)
                        throw new CodeParsingException("linker error: module " + moduleName + " not found", lineNumber.get(), linesCopy);
                    ConfigurationTypedObject object = null;
                    for(ConfigurationTypedObject configurationTypedObject : module.getModuleConfigurations()) {
                        if(objectName.equals(configurationTypedObject.getName()))
                            object = configurationTypedObject;
                    }
                    if(object == null)
                        throw new CodeParsingException("linker error: module object " + objectName + " not found", lineNumber.get(), linesCopy);
                    parentObj = object;
                }

                if(hasConfig) {
                    CustomEndpointSection.UseDirective directive = parseUseDirectiveConfigured(name, parentObj, "{", lines, lineNumber, linesCopy, preparedDefines);
                    section.useDirectives.add(directive);
                } else {
                    section.useDirectives.add(new UseDirectiveImpl(name, parentObj));
                }
            } else
                throw new CodeParsingException("custom section parse error: unknown code phrase", lineNumber.get(), linesCopy);
        }

        return section;
    }

    private CustomEndpointSection.UseDirective parseUseDirectiveConfigured(String name, ConfigurationTypedObject parent, String line, Iterator<String> lines, AtomicInteger lineCounter, List<String> linesCopy, Map<Pattern, String> preparedDefinitions) {
        List<CustomEndpointSection.CustomProperty> customProperties = new ArrayList<>();

        TypedObjectImpl object = new TypedObjectImpl(name, parent == null ? "" : parent.getType());
        if(parent != null)
            object.getEntries().putAll(parent.getEntries());

        boolean finish = false;
        while(!finish) {
            String l = buildDefines(lines.next(), preparedDefinitions);
            lineCounter.incrementAndGet();
            lines.remove();

            if(l.replaceAll("\\s", "").startsWith("}")) {//Close object
                finish = true;
                break;
            }

            while(!l.isEmpty()) {
                if(l.contains(":")) {
                    ConfigElement element = parseElement(l, lines, linesCopy, lineCounter, preparedDefinitions, "");
                    object.append(element.element);
                    l = l.substring(element.text.length());//Remove last element
                } else {
                    CustomPropertyImpl prop = parseCustomProperty(l, lines, linesCopy, lineCounter, preparedDefinitions);
                    customProperties.add(prop);
                    l = "";
                }
                String l1;
                if(!l.isEmpty())
                    l1 = l.substring(1).replaceFirst("^\\s*", ""); //Remove spaces
                else
                    l1 = "";
                l = l1;

                if(l.startsWith("}")) {//Close object
                    finish = true;
                    break;
                }
            }
        }
        if(!finish)
            throw new CodeParsingException("use directive parse error: object doesn't have the end!", lineCounter.get(), linesCopy);

        UseDirectiveImpl useDirective = new UseDirectiveImpl(name, object);
        useDirective.customProperties.addAll(customProperties);
        return useDirective;
    }

    private CustomPropertyImpl parseCustomProperty(String l, Iterator<String> lines, List<String> linesCopy, AtomicInteger lineCounter, Map<Pattern, String> preparedDefines) {
        String name;
        {
            String[] parts = l.replaceFirst("^\\s*", "").split(" ", 2);
            if(parts.length != 2)
                throw new CodeParsingException("custom property parse error: invalid declaration", lineCounter.get(), linesCopy);
            name = parts[0];
            if(StringUtils.isWhitespace(name))
                throw new CodeParsingException("custom property parse error: name is empty", lineCounter.get(), linesCopy);
            String semicolon = parts[1].replaceFirst("^\\s*", "");
            if(!semicolon.startsWith("{"))
                throw new CodeParsingException("custom property parse error: { expected", lineCounter.get(), linesCopy);
            else if(semicolon.startsWith("{}"))
                return new CustomPropertyImpl(name);
            else if(!StringUtils.isWhitespace(semicolon.substring(1)))
                throw new CodeParsingException("custom property error: unexpected symbols after {", lineCounter.get(), linesCopy);
        }

        CustomPropertyImpl customProperty = new CustomPropertyImpl(name);
        boolean in = true;
        while(in) {
            String line = buildDefines(cutLine(lines), preparedDefines).replaceFirst("^\\s*", "");
            lineCounter.incrementAndGet();
            if(StringUtils.isWhitespace(line))
                continue;

            if(line.startsWith("}")) {
                if(!StringUtils.isWhitespace(line.substring(1)))
                    throw new CodeParsingException("custom property parse error: symbols after }", lineCounter.get(), linesCopy);
                in = false;
                break;
            }

            String[] parts = line.split(" ", 3);
            if(!parts[0].equals("use"))
                throw new CodeParsingException("use command parse error: unexpected symbols", lineCounter.get(), linesCopy);
            if(parts.length < 2)
                throw new CodeParsingException("use command parse error: invalid declaration (must be 'use name')", lineCounter.get(), linesCopy);
            if(parts[1].isEmpty())
                throw new CodeParsingException("use command parse error: referent is empty", lineCounter.get(), linesCopy);
            customProperty.useCommands.add(new UseCommandImpl(parts[1], parts.length < 3 ? "" : parts[2]));
        }

        return customProperty;
    }

    private ConfigurationModule findModule(@Nonnull String name, @Nonnull Collection<ConfigurationModuleImpl> modules) {
        for(ConfigurationModule module : modules) {
            if(name.equals(module.getName()))
                return module;
        }
        for(ConfigurationModule module : modules) {
            ConfigurationModule module1 = findModuleInternal(name, module);
            if(module1 != null)
                return module1;
        }
        return null;
    }

    private ConfigurationModule findModuleInternal(String name, ConfigurationModule configurationModule) {
        for(ConfigurationModule module : configurationModule.getModules()) {
            if(name.equals(module.getName()))
                return module;
        }
        for(ConfigurationModule module : configurationModule.getModules()) {
            ConfigurationModule f = findModuleInternal(name, module);
            if(f != null)
                return f;
        }
        return null;
    }

    @Nonnull
    protected List<String> cutHeader(@Nonnull Iterator<String> linesIterator, Predicate<String> whereCut, boolean inclusive) {
        List<String> headerLines = new ArrayList<>();
        while(linesIterator.hasNext()) {
            String line = linesIterator.next();
            if(inclusive)
                linesIterator.remove();
            if(whereCut.test(line))
                break;
            if(!inclusive)
                linesIterator.remove();
            headerLines.add(line);
        }
        return headerLines;
    }

    protected List<File> extractModules(@Nonnull Iterator<String> linesIterator, @Nonnull Path relative, @Nonnull List<String> file, int lineOff) {
        List<File> modules = new ArrayList<>();
        int lineNum = 1;
        while(linesIterator.hasNext()) {
            String line = linesIterator.next().replaceAll("^\\s*", "");
            line = COMMENT_PATTERN.matcher(line).replaceAll("");
            if(line.startsWith("include")) {
                Matcher matcher = INCLUDE_REGEX.matcher(line);
                if(!matcher.find())
                    throw new CodeParsingException("include statement error: doesn't match the RegExp", lineNum + lineOff, file);
                String fileName = matcher.group("inc");
                if(fileName == null)
                    throw new CodeParsingException("include statement error: this statement doesn't have file definition!", lineNum + lineOff, file);

                File theFile = relative.resolve(fileName).toFile();
                if(!fileReader.exists(theFile))
                    throw new CodeParsingException("include statement error: file " + theFile + " not found!", lineNum + lineOff, file);
                if(!fileReader.isFile(theFile))
                    throw new CodeParsingException("include statement error: file " + theFile + " is a directory!", lineNum + lineOff, file);
                if(!fileReader.canRead(theFile))
                    throw new CodeParsingException("include statement error: file " + theFile + " is not readable!", lineNum + lineOff, file);
                if(!fileReader.canExecute(theFile))
                    throw new CodeParsingException("include statement error: file " + theFile + " is not executable!", lineNum + lineOff, file);
                modules.add(theFile);
            }
            lineNum++;
        }
        return modules;
    }

    @Nonnull
    protected List<ExternalLanguageHelper> extractHelpers(@Nonnull Iterator<String> linesIterator, @Nonnull Path relative, @Nonnull List<String> file, int lineOff) {
        List<ExternalLanguageHelper> helpers = new ArrayList<>();
        int lineNum = 1;
        while(linesIterator.hasNext()) {
            String line = linesIterator.next().replaceAll("^\\s*", "");
            line = COMMENT_PATTERN.matcher(line).replaceAll("");
            if(line.startsWith("use")) {
                Matcher matcher = USE_REGEX.matcher(line);
                String fileName = null;
                String languageName = null;
                String contextName = null;
                while(matcher.find()) {
                    if(matcher.group("use") != null)
                        fileName = matcher.group("use");
                    else if(matcher.group("lang") != null)
                        languageName = matcher.group("lang");
                    else if(matcher.group("ctx") != null)
                        contextName = matcher.group("ctx");
                }
                if(languageName == null)
                    throw new CodeParsingException("use statement error: this statement doesn't have language definition!", lineNum + lineOff, file);
                if(fileName == null)
                    throw new CodeParsingException("use statement error: this statement doesn't have file definition!", lineNum + lineOff, file);

                File theFile = relative.resolve(fileName).toFile();
                if(!fileReader.exists(theFile))
                    throw new CodeParsingException("use statement error: file " + theFile + " not found!", lineNum + lineOff, file);
                if(!fileReader.isFile(theFile))
                    throw new CodeParsingException("use statement error: file " + theFile + " is a directory!", lineNum + lineOff, file);
                if(!fileReader.canRead(theFile))
                    throw new CodeParsingException("use statement error: file " + theFile + " is not readable!", lineNum + lineOff, file);

                ExecutionContext ctx = ExecutionContext.parse(contextName);
                if(ctx == null)
                    throw new CodeParsingException("use statement error: context " + contextName + " is not supported!", lineNum + lineOff, file);

                ExternalLanguageHelper helper = new ExternalLanguageHelperImpl(theFile, languageName, ctx);
                helpers.add(helper);
            }
            lineNum++;
        }
        return helpers;
    }

    protected ConfigurationModuleImpl parseModule(File file, @Nonnull ConfigParserInstructionsHeader endpointHeader) {
        Path folder = file.getParentFile().toPath();
        List<String> lines = fileReader.readFile(file.toPath());
        List<String> linesCopy = new ArrayList<>(lines);

        if(lines.isEmpty())
            throw new CodeParsingException("Module doesn't have any line to parse!", 0, lines);
        List<String> headerLines;
        if(lines.get(0).replaceFirst("^\\s*", "").startsWith("#"))
            headerLines = cutHeader(lines.iterator(), StringUtils::isWhitespace, true);
        else
            headerLines = Collections.emptyList();
        List<String> includeLines = cutHeader(lines.iterator(), s -> s.startsWith("module"), false);
        List<ExternalLanguageHelper> helpers = extractHelpers(includeLines.iterator(), folder, linesCopy, headerLines.size());
        List<File> moduleFiles = extractModules(includeLines.iterator(), folder, linesCopy, headerLines.size());
        Map<String, ConfigurationModuleImpl> modules = moduleFiles.stream()
                .map(file1 -> parseModule(file1, endpointHeader))
                .collect(Collectors.toMap(ConfigurationModuleImpl::getName, o -> o));
        ConfigParserInstructionsHeader header = modules.values().stream()
                .map(configurationModule -> configurationModule.defines)
                .reduce((header1, header2) -> {
                    header1.append(header2);
                    return header1;
                })
                .orElse(null);
        if(header != null)
            header.append(endpointHeader);
        ConfigParserInstructionsHeader myHeader = ConfigParserInstructionsHeader.parse(headerLines, header, fileReader);
        Map<String, String> map = myHeader.getAllDefines();
        Map<Pattern, String> preparedDefines = new HashMap<>();
        map.forEach((s, s2) -> preparedDefines.put(Pattern.compile(s), s2));

        String firstLine = buildDefines(cutLine(lines.iterator()), preparedDefines);
        int actualCodeStartLine = headerLines.size() + includeLines.size();
        String moduleName;
        {
            int off = firstLine.indexOf("module ") + "module ".length();
            int cutOff = firstLine.lastIndexOf(" {");

            if(off == -1)
                throw new CodeParsingException("Unexpected symbol: module expected", actualCodeStartLine + 1, linesCopy);
            if(cutOff == -1)
                throw new CodeParsingException("Unexpected symbol: { expected", actualCodeStartLine, linesCopy);
            moduleName = firstLine.substring(off, cutOff).replace(" ", "");
        }
        int level = 0;
        List<ConfigurationTypedObject> objects = new ArrayList<>();
        ConfigurationTypedObject currentObject = null;
        AtomicInteger lineNumber = new AtomicInteger(actualCodeStartLine + 1);
        while(!lines.isEmpty()) {
            String line = buildDefines(cutLine(lines.iterator()), preparedDefines);
            lineNumber.getAndIncrement();
            if(StringUtils.isWhitespace(line))
                continue;

            if(!line.contains("{") && CLOSE_BRACKET_REGEX_PREDICATE.test(line)) {
                level--;
                if(currentObject == null)
                    break;
                else {
                    objects.add(currentObject);
                    currentObject = null;
                }
            } else if(level == 0) {//Expect named object start
                String desc;
                ConfigurationTypedObject extend = null;
                if(line.contains(" extends ")) {
                    String[] parts = line.split(" extends ", 2);
                    if(parts.length < 2)
                        throw new CodeParsingException("extends syntax error", lineNumber.get(), linesCopy);
                    desc = parts[0].replaceAll("^\\s*", "");
                    String[] extendType = parts[1].replaceAll("^\\s*", "").split("\\.", 2);
                    if(extendType.length < 2)
                        throw new CodeParsingException("extend supertype not found: incorrect declaration", lineNumber.get(), linesCopy);
                    String targetModule = extendType[0];
                    String supertype = extendType[1]; //Supertype name and {
                    if(!supertype.endsWith("{"))
                        throw new CodeParsingException("type definition error: { expected", lineNumber.get(), linesCopy);
                    supertype = supertype.substring(0, supertype.length() - 1).replace(" ", "");
                    if(targetModule.isEmpty() || supertype.isEmpty())
                        throw new CodeParsingException("extend syntax error: supertype definition is invalid", lineNumber.get(), linesCopy);
                    if(!modules.containsKey(targetModule))
                        throw new CodeParsingException("module " + targetModule + " not found", lineNumber.get(), linesCopy);
                    ConfigurationModule module = findModule(targetModule, modules.values());
                    if(module == null)
                        throw new CodeParsingException("linker error: extend supermodule " + targetModule + " not found", lineNumber.get(), linesCopy);
                    for(ConfigurationTypedObject object : module.getModuleConfigurations()) {
                        if(object.getName().equals(supertype))
                            extend = object;
                    }
                    if(extend == null)
                        throw new CodeParsingException("linker error: extend supertype object" + supertype + " not found", lineNumber.get(), linesCopy);
                } else {
                    String l = line.replace(" ", "");
                    if(!l.endsWith("{"))
                        throw new CodeParsingException("type definition error: { expected", lineNumber.get(), linesCopy);
                    desc = l.substring(0, l.length() - 1);
                }
                TypedObjectImpl object;
                if(extend == null)
                    object = TypedObjectImpl.parse(desc);
                else
                    object = TypedObjectImpl.parse(desc, extend);
                if(object == null)
                    throw new CodeParsingException("type definition error: bad description(expected name:type)", lineNumber.get(), linesCopy);

                currentObject = object;
                level++;
            } else {
                ConfigurationElement element = parseElement(line, lines.iterator(), linesCopy, lineNumber, preparedDefines, "").element;
                if(currentObject == null)
                    throw new CodeParsingException("unexpected error: current object == null", lineNumber.get(), linesCopy);
                currentObject.getEntries().put(element.getName(), element);
            }
        }
        if(!lines.isEmpty()) {
            for(String line : lines) {
                if(!StringUtils.isWhitespace(line))
                    throw new CodeParsingException("Can't recognize symbols outside of the module!", lineNumber.get(), linesCopy);
            }
        }
        return new ConfigurationModuleImpl(moduleName, new ArrayList<>(modules.values()), helpers, myHeader, objects);
    }

    private ConfigElement parseElement(String currentLine, Iterator<String> lines, List<String> lineCopy, AtomicInteger lineCounter, Map<Pattern, String> preparedDefinitions, String nameOverride) {
        return parseElement(currentLine, lines, lineCopy, lineCounter, preparedDefinitions, nameOverride, false);
    }

    private ConfigElement parseElement(String currentLine, Iterator<String> lines, List<String> lineCopy, AtomicInteger lineCounter, Map<Pattern, String> preparedDefinitions, String nameOverride, boolean unknownTypeIsString) {
        String[] parts;
        String name;
        if(nameOverride.isEmpty()) {
            parts = currentLine.split(":", 2);
            if(parts.length < 2)
                throw new CodeParsingException("property definition error: bad definition(expected name: def)", lineCounter.get(), lineCopy);
            name = parts[0].replace(" ", "");
        } else {
            parts = new String[]{"", currentLine};
            name = nameOverride;
        }
        if(name.isEmpty())
            throw new CodeParsingException("property definition error: name can't be empty", lineCounter.get(), lineCopy);
        String def = parts[1].replaceAll("^\\s*", "");//Delete all spaces from start
        String defSpaces = parts[1].replace(def, "");
        if(def.isEmpty())
            throw new CodeParsingException("property definition error: expected any symbol after :", lineCounter.get(), lineCopy);
        if(def.startsWith("\"") || def.startsWith("\'")) { //string/char
            StringBuilder stringBuilder = new StringBuilder();
            boolean quoted = def.startsWith("\'");
            char[] l = def.substring(1).toCharArray();
            boolean stop = false;
            while(!stop) {
                for(char c : l) {
                    if(quoted && c == '\'') {
                        stop = true;
                        break;
                    } else if(!quoted && c == '\"') {
                        stop = true;
                        break;
                    } else
                        stringBuilder.append(c);
                }
                if(!stop) {
                    stringBuilder.append('\n');
                    l = buildDefines(lines.next(), preparedDefinitions).toCharArray();
                    lineCounter.incrementAndGet();
                    lines.remove();
                }
            }
            char quote = def.startsWith("\"") ? '"' : '\'';
            if(stringBuilder.length() == 1 && quoted)
                return new ConfigElement(PrimitiveImpl.wrap(name, stringBuilder.toString().charAt(0)), parts[0] + ':' + defSpaces + quote + stringBuilder.toString().charAt(0) + quote);
            else
                return new ConfigElement(PrimitiveImpl.wrap(name, stringBuilder.toString()), parts[0] + ':' + defSpaces + quote + stringBuilder.toString() + quote);
        } else if(def.startsWith("<")) {//Code fragment
            String langName = def.substring(1, def.indexOf('>'));
            String closeTag = "</" + langName + '>';
            String codeFragment = def.substring(def.indexOf('>') + 1);
            if(codeFragment.contains(closeTag)) {
                String code = codeFragment.substring(0, codeFragment.indexOf(closeTag));
                return new ConfigElement(new CodeImpl(langName, code, name), parts[0] + ':' + defSpaces + '<' + langName + '>' + code + closeTag);
            } else {
                StringBuilder code = new StringBuilder();
                code.append(codeFragment);
                String l = buildDefines(cutLine(lines), preparedDefinitions);
                lineCounter.incrementAndGet();
                while(!l.contains(closeTag)) {
                    code.append('\n');
                    code.append(l);
                    l = buildDefines(cutLine(lines), preparedDefinitions);
                    lineCounter.incrementAndGet();
                }
                String closeLine = l.substring(0, l.indexOf(closeTag));
                if(!StringUtils.isWhitespace(closeLine)) {
                    code.append('\n');
                    code.append(closeLine);
                }
                return new ConfigElement(new CodeImpl(langName, code.toString(), name), parts[0] + ':' + defSpaces + '<' + langName + '>' + code + closeTag);
            }
        } else if(def.endsWith("()")) {
            String l = def.split(",", 2)[0].replace(" ", "");//Ensure that function name won't have another objects
            return new ConfigElement(new FunctionImpl(name, l.substring(0, l.length() - 2)), parts[0] + ':' + defSpaces + def);
        } else if(def.startsWith("{")) { //Object
            ObjectImpl object = new ObjectImpl(name);
            boolean finish = false;
            String content = def.replaceFirst("\\{\\s*", ""); //Remove start bracket

            StringBuilder text = new StringBuilder(parts[0] + ':' + defSpaces);
            text.append(def, 0, def.length() - content.length());

            if(!content.isEmpty()) {
                content = content.replaceFirst("^\\s*", "");
                while(!content.isEmpty()) {
                    ConfigElement element = parseElement(content, lines, lineCopy, lineCounter, preparedDefinitions, "");
                    object.append(element.element);
                    content = content.substring(element.text.length());//Remove last element
                    String content1;
                    if(!content.isEmpty())
                        content1 = content.substring(1).replaceFirst("^\\s*", ""); //Remove comma and spaces
                    else
                        content1 = "";
                    text.append(element.text);
                    text.append(content, 0, content.length() - content1.length());
                    content = content1;
                    if(content.startsWith("}")) {//Close object
                        text.append("}");
                        finish = true;
                        break;
                    }
                }
            }
            while(!finish) {
                String l = buildDefines(cutLine(lines), preparedDefinitions);
                lineCounter.incrementAndGet();

                if(l.replaceAll("\\s", "").startsWith("}")) {//Close object
                    text.append('\n');
                    text.append("}");
                    finish = true;
                    break;
                }

                while(!l.isEmpty()) {
                    ConfigElement element = parseElement(l, lines, lineCopy, lineCounter, preparedDefinitions, "");
                    object.append(element.element);
                    String[] textLines = element.text.split("\\n");
                    if(textLines.length >= 1)
                        l = l.substring(textLines[0].length());//Remove last element, all children element's lines already cut

                    String l1;
                    if(!l.isEmpty())
                        l1 = l.substring(1).replaceFirst("^\\s*", ""); //Remove spaces
                    else
                        l1 = "";

                    text.append('\n');
                    text.append(element.text);
                    text.append(l, 0, l.length() - l1.length());
                    l = l1;

                    if(l.startsWith("}")) {//Close object
                        text.append("}");
                        finish = true;
                        break;
                    }
                }
            }
            if(!finish)
                throw new CodeParsingException("Object doesn't have the end!", lineCounter.get(), lineCopy);
            return new ConfigElement(object, text.toString());
        } else if(def.startsWith("[")) {
            ArrayImpl array = new ArrayImpl(name);
            StringBuilder text = new StringBuilder(parts[0] + ':' + defSpaces + "[");
            boolean finish = false;
            String last = def.substring(1);
            if(!last.isEmpty()) {
                while(!last.isEmpty()) {
                    ConfigElement element = parseElement(last, lines, lineCopy, lineCounter, preparedDefinitions, Integer.toString(array.size()));
                    array.append(element.element);
                    String[] textLines = element.text.split("\\n");
                    if(textLines.length >= 1)
                        last = last.substring(textLines[0].length());//Remove last element, all children element's lines already cut
                    String l1;
                    if(!last.isEmpty())
                        l1 = last.substring(1).replaceFirst("^\\s*", ""); //Remove spaces
                    else
                        l1 = "";
                    text.append(element.text);
                    text.append(last, 0, last.length() - l1.length());
                    last = l1;
                    if(StringUtils.isWhitespace(last) || last.matches("\\s*,\\s*"))
                        break;//Nothing interesting in the string

                    if(last.startsWith("]")) { //Close arr
                        text.append("]");
                        finish = true;
                        break;
                    }
                }
            }
            while(!finish) {
                String l = buildDefines(cutLine(lines), preparedDefinitions);
                lineCounter.incrementAndGet();

                if(l.replaceAll("\\s", "").startsWith("]")) { //Close arr
                    text.append("]");
                    finish = true;
                    break;
                }

                while(!l.isEmpty()) {
                    ConfigElement element = parseElement(l, lines, lineCopy, lineCounter, preparedDefinitions, Integer.toString(array.size()));
                    array.append(element.element);
                    String text1 = element.text.replaceFirst(":", "");//Remove empty name
                    l = l.substring(text1.length());//Remove last element
                    String l1;
                    if(!l.isEmpty())
                        l1 = l.substring(1).replaceFirst("^\\s*", ""); //Remove spaces
                    else
                        l1 = "";
                    text.append(text1);
                    text.append(l, 0, l.length() - l1.length());
                    l = l1;

                    if(l.startsWith("]")) { //Close arr
                        text.append("]");
                        finish = true;
                        break;
                    }
                }
            }
            if(!finish)
                throw new CodeParsingException("Array doesn't have the end!", lineCounter.get(), lineCopy);
            return new ConfigElement(array, text.toString());
        } else {
            String real = def.split("[,|\\s]", 2)[0];
            ConfigurationElement element = PrimitiveImpl.parseNotString(name, real);
            if(element == null) {
                if(unknownTypeIsString)
                    return new ConfigElement(PrimitiveImpl.wrap(name, real), parts[0] + ':' + defSpaces + real);
                throw new CodeParsingException("Can't parse definition!", lineCounter.get(), lineCopy);
            }
            return new ConfigElement(element, (nameOverride != null && !nameOverride.isEmpty()) ? defSpaces + real : parts[0] + ':' + defSpaces + real);
        }
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class ConfigElement {
        private final ConfigurationElement element;
        private final String text;
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    @RequiredArgsConstructor
    private static final class ConfigurationEndpointImpl implements ConfigurationEndpoint {
        private final String name;
        @Nonnull
        private final ApiEndpointSection apiEndpointSection;
        @Nonnull
        private final ScriptEndpointSection scriptEndpointSection;
        private final List<ExternalLanguageHelper> helpers;
        private final List<ConfigurationModule> modules;
        private final Map<String, CustomEndpointSection> sections;

        @Override
        public CustomEndpointSection getCustomSection(String name) {
            return sections.get(name);
        }

        @Override
        public ApiEndpointSection getApiSection() {
            return apiEndpointSection;
        }

        @Override
        public ScriptEndpointSection getScriptSection() {
            return scriptEndpointSection;
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static final class ScriptEndpointSectionImpl implements ScriptEndpointSection {
        private final Map<String, ScriptSection> sections;

        @Override
        public ScriptSection getSection(String name) {
            return sections.get(name);
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class ScriptSectionImpl implements ScriptEndpointSection.ScriptSection {
        private final List<ScriptEndpointSection.Command> commands = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class CommandImpl implements ScriptEndpointSection.Command {
        private final String name;
        private final ConfigurationElement arg;
    }

    @RequiredArgsConstructor
    @ToString
    private static final class ApiEndpointSectionImpl implements ApiEndpointSection {
        private final ConfigurationObject object;

        @Nullable
        @Override
        public ConfigurationElement getElement(String name) {
            return object.getElement(name);
        }

        @Nullable
        @Override
        public <V extends ConfigurationElement> V getElement(String name, Class<V> clazz) {
            return object.getElement(name, clazz);
        }

        @Override
        public Set<String> getNames() {
            return object.getNames();
        }

        @Override
        public Map<String, ConfigurationElement> getEntries() {
            return object.getEntries();
        }

        @Override
        public int getSize() {
            return object.getSize();
        }

        @Override
        public boolean hasKey(String name) {
            return object.hasKey(name);
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class CustomEndpointSectionImpl implements CustomEndpointSection {
        private final List<UseDirective> useDirectives = new ArrayList<>();
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static final class UseDirectiveImpl implements CustomEndpointSection.UseDirective {
        private final String name;
        private final ConfigurationTypedObject configuration;
        private final List<CustomEndpointSection.CustomProperty> customProperties = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class CustomPropertyImpl implements CustomEndpointSection.CustomProperty {
        private final String name;
        private final List<CustomEndpointSection.UseCommand> useCommands = new ArrayList<>();
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class UseCommandImpl implements CustomEndpointSection.UseCommand {
        private final String referent;
        @Nullable
        private final String arg;
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static final class ConfigurationModuleImpl implements ConfigurationModule {
        private final String name;
        private final List<ConfigurationModule> modules;
        private final List<ExternalLanguageHelper> helpers;
        private final ConfigParserInstructionsHeader defines;
        private final List<ConfigurationTypedObject> moduleConfigurations;
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    private static final class ExternalLanguageHelperImpl implements ExternalLanguageHelper {
        private final File helperFile;
        private final String language;
        private final ExecutionContext context;
    }
}

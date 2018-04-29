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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigurationParser {
    protected static final Pattern COMMENT_PATTERN = Pattern.compile("//.*");
    protected static final Pattern USE_REGEX = Pattern.compile("use '(?<use>.*?)'| language '(?<lang>.*?)'| context '(?<ctx>.*?)'");
    protected static final Pattern INCLUDE_REGEX = Pattern.compile("include '(?<inc>.*?)'");
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

    public ConfigurationEndpoint parse() throws IOException {
        List<String> endpoint = fileReader.readFile(this.endpoint.toPath());
        List<String> endpointClone = new ArrayList<>(endpoint);

        if(endpoint.isEmpty())
            throw new CodeParsingException("Endpoint file is empty!", 0, endpoint);

        List<String> headerLines;
        if(!endpoint.get(0).replaceAll("^\\s*", "").startsWith("#"))
            headerLines = new ArrayList<>();
        else
            headerLines = cutHeader(endpoint.iterator(), StringUtils::isWhitespace, true);
        ConfigParserInstructionsHeader header = ConfigParserInstructionsHeader.parse(headerLines, null, fileReader);

        List<String> includeLines = cutHeader(endpoint.iterator(), s -> s.startsWith("endpoint"), false);
        List<ExternalLanguageHelper> helpers = extractHelpers(includeLines.iterator(), folder, endpointClone, headerLines.size());
        List<File> moduleFiles = extractModules(includeLines.iterator(), folder, endpointClone, headerLines.size());

        List<ConfigurationModuleImpl> modules = moduleFiles.stream()
                .map(file -> parseModule(file, header))
                .collect(Collectors.toList());


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
                if(!fileReader.canExecute(theFile))
                    throw new CodeParsingException("use statement error: file " + theFile + " is not executable!", lineNum + lineOff, file);

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
        if(lines.get(0).replaceAll("^\\s*", "").startsWith("#"))
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
                    ConfigurationModule module = modules.get(targetModule);
                    for(ConfigurationTypedObject object : module.getModuleConfigurations()) {
                        if(object.getName().equals(supertype))
                            extend = object;
                    }
                    if(extend == null)
                        throw new CodeParsingException("extend error: supertype not found", lineNumber.get(), linesCopy);
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
                String l;
                while(!(l = lines.next()).contains(closeTag)) {
                    code.append('\n');
                    code.append(l);
                    lineCounter.incrementAndGet();
                    lines.remove();
                }
                String closeLine = l.substring(0, l.indexOf(closeTag));
                lines.remove();//Remove code line
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

            StringBuilder text = new StringBuilder(parts[0] + ':' + defSpaces + "{");
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
                String l = buildDefines(lines.next(), preparedDefinitions);
                lineCounter.incrementAndGet();
                lines.remove();

                if(l.replaceAll("\\s", "").startsWith("}")) {//Close object
                    text.append("}");
                    finish = true;
                    break;
                }

                while(!l.isEmpty()) {
                    ConfigElement element = parseElement(l, lines, lineCopy, lineCounter, preparedDefinitions, "");
                    object.append(element.element);
                    l = l.substring(element.text.length());//Remove last element
                    String l1;
                    if(!l.isEmpty())
                        l1 = l.substring(1).replaceFirst("^\\s*", ""); //Remove spaces
                    else
                        l1 = "";

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
                    last = last.substring(element.text.length() - 1);//Remove last element
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
                String l = buildDefines(lines.next(), preparedDefinitions);
                lineCounter.incrementAndGet();
                lines.remove();

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
            if(element == null)
                throw new CodeParsingException("Can't parse definition!", lineCounter.get(), lineCopy);
            return new ConfigElement(element, parts[0] + ':' + defSpaces + real);
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
    @RequiredArgsConstructor
    private static final class ConfigurationEndpointImpl implements ConfigurationEndpoint {
        private final String name;
        private final List<ExternalLanguageHelper> helpers = new ArrayList<>();
        private final List<ConfigurationModule> modules = new ArrayList<>();

        @Override
        public CustomEndpointSection getCustomSection(String name) {
            return null;
        }

        @Override
        public ApiEndpointSection getApiSection() {
            return null;
        }

        @Override
        public ScriptEndpointSection getScriptSection() {
            return null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static final class ConfigurationModuleImpl implements ConfigurationModule {
        private final String name;
        private final List<ConfigurationModule> modules;
        private final List<ExternalLanguageHelper> helpers;
        private final ConfigParserInstructionsHeader defines;
        private final List<ConfigurationTypedObject> moduleConfigurations;
    }

    @RequiredArgsConstructor
    @Getter
    private static final class ExternalLanguageHelperImpl implements ExternalLanguageHelper {
        private final File helperFile;
        private final String language;
        private final ExecutionContext context;
    }
}

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

import com.alesharik.webserver.configuration.config.ext.DefineEnvironment;
import com.alesharik.webserver.configuration.config.ext.DefineManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ConfigParserInstructionsHeader implements DefineEnvironment {//FIXME DefineManager
    static final String WHITESPACE_REGEX = "^\\s*(?=\\S|$)";
    @Getter
    private final Map<String, String> defines = new HashMap<>();

    private ConfigParserInstructionsHeader() {
    }

    public static ConfigParserInstructionsHeader parse(@Nonnull List<String> list, @Nullable ConfigParserInstructionsHeader resolver, @Nonnull FileReader fileReader) {
        if(Constants.PRINT_CONFIG)
            System.out.println("Started!");
        ConfigParserInstructionsHeader me = new ConfigParserInstructionsHeader();
        SharedEnv env = new SharedEnv(me, resolver);
        int depth = 0;
        int cutDepth = Integer.MAX_VALUE;
        Deque<String> queue = new ArrayDeque<>(list);
        String s;

        List<String> file = new ArrayList<>(list);
        int line = -1;

        while((s = queue.poll()) != null) {
            line++;
            if(Constants.PRINT_CONFIG)
                System.out.println(line + " | " + s);
            s = s.replaceAll(WHITESPACE_REGEX, "");
            if(s.startsWith("//"))
                continue;
            if(s.startsWith("#endif")) {
                depth--;
                if(depth < cutDepth)
                    cutDepth = Integer.MAX_VALUE;

                if(depth < 0)
                    throw new CodeParsingException("flow error: #endif can't close not-existent #if block!", line, file);
            }

            if(s.startsWith("#define")) {
                if(depth >= cutDepth)
                    continue;

                String main = s.substring(7).replaceAll(WHITESPACE_REGEX, "");//#define - 7 chars
                String[] parts = main.split("\\s(?='.*?')");
                if(parts.length != 2)
                    throw new CodeParsingException("#define syntax error: expected 'REGEX' 'REPL' pattern!", line, file);
                String regex = parts[0].substring(1, parts[0].length() - 1);
                String replacement = parts[1].substring(1, parts[1].length() - 1);
                me.defines.put(regex, replacement);
            } else if(s.startsWith("#ifdef")) {
                depth++;
                if(depth >= cutDepth)
                    continue;
                String id = s.substring(6).replaceAll(WHITESPACE_REGEX, "");//#ifdef - 6 chars
                if(id.isEmpty())
                    throw new CodeParsingException("#idfef syntax error: expected one parameter!", line, file);
                if(!me.defines.containsKey(id) && (resolver == null || !resolver.defines.containsKey(id)) && !DefineManager.isDefined(id, env))
                    cutDepth = depth;
            } else if(s.startsWith("#ifndef")) {
                depth++;
                if(depth >= cutDepth)
                    continue;
                String id = s.substring(7).replaceAll(WHITESPACE_REGEX, "");//#ifndef - 7 chars
                if(id.isEmpty())
                    throw new CodeParsingException("#idnfef syntax error: expected one parameter!", line, file);
                if(me.defines.containsKey(id) || (resolver != null && resolver.defines.containsKey(id)) || DefineManager.isDefined(id, env))
                    cutDepth = depth;
            } else if(s.startsWith("#include")) {
                if(depth >= cutDepth)
                    continue;
                String f = s.substring(8).replaceAll(WHITESPACE_REGEX, "");//#include - 8 chars
                if(f.isEmpty())
                    throw new CodeParsingException("#include syntax error: expected one parameter!", line, file);
                List<String> lines = fileReader.readFile(new File(f).toPath());
                for(int i = lines.size() - 1; i >= 0; i--)
                    queue.addFirst(lines.get(i));
                file.addAll(line + 1 >= file.size() ? line : line + 1, lines);
            }
        }
        if(depth > 0)
            throw new CodeParsingException("flow error: #if block not closed!", line, file);
        //Debug only:
        if(Constants.PRINT_CONFIG) {
            System.out.println("Representation: ");
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < file.size(); i++) {
                builder.append(i);
                builder.append(" | ");
                builder.append(file.get(i));
                builder.append('\n');
            }
            System.out.println(builder.toString());
        }

        return me;
    }

    public Map<String, String> getAllDefines() {
        Map<String, String> ret = new HashMap<>(defines);
        ret.putAll(DefineManager.getAllDefines(this));
        return ret;
    }

    /**
     * Need to start from modules to endpoint
     *
     * @param header the file header
     */
    public void append(@Nonnull ConfigParserInstructionsHeader header) {
        defines.putAll(header.defines);
    }

    @Nullable
    @Override
    public String getDefinition(@Nonnull String name) {
        if(defines.containsKey(name))
            return defines.get(name);
        return DefineManager.getDefinition(name, this);
    }

    @Override
    public boolean isDefined(@Nonnull String name) {
        return defines.containsKey(name) || DefineManager.isDefined(name, this);
    }

    @Override
    public boolean isProvided(@Nonnull String name) {
        if(defines.containsKey(name))
            return false;
        return DefineManager.isDefined(name, this);
    }

    @RequiredArgsConstructor
    static final class SharedEnv implements DefineEnvironment {
        private final ConfigParserInstructionsHeader a;
        private final ConfigParserInstructionsHeader b;

        @Nullable
        @Override
        public String getDefinition(@Nonnull String name) {
            return a.isDefined(name) ? a.getDefinition(name) : (b == null ? null : b.getDefinition(name));
        }

        @Override
        public boolean isDefined(@Nonnull String name) {
            return a.isDefined(name) || b == null || b.isDefined(name);
        }

        @Override
        public boolean isProvided(@Nonnull String name) {
            return !(!a.isProvided(name) && (b == null || !b.isProvided(name)));
        }
    }
}

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

import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ConfigurationModule;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ExecutionContext;
import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationFunctionElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationParserTest {
    @Test
    public void parseModule() {
        ConfigurationParser parser = new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module1.module"), fileReader());
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule mod = parser.parseModule(new File("com/alesharik/webserver/configuration/config/lang/parser/module1.module"), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule module = mod;

        assertEquals("test", module.getName());
        ConfigurationTypedObject object = module.getModuleConfigurations().get(0);
        assertEquals("e", object.getName());
        assertEquals("e", object.getType());

        ConfigurationPrimitive primitive = (ConfigurationPrimitive) object.getElement("e");
        assertEquals("e", primitive.getName());
        assertEquals(1, ((ConfigurationPrimitive.Int) primitive).value());

        ConfigurationObject object1 = (ConfigurationObject) object.getElement("b");

        assertEquals("b", object1.getName());
        {
            ConfigurationPrimitive primitive1 = (ConfigurationPrimitive) object1.getElement("e");
            assertEquals("e", primitive1.getName());
            assertEquals(1, ((ConfigurationPrimitive.Int) primitive1).value());

            ConfigurationPrimitive primitive2 = (ConfigurationPrimitive) object1.getElement("b");
            assertPrimitiveEquals(primitive2, "b", 2);
        }

        ConfigurationObjectArray array = (ConfigurationObjectArray) object.getElement("c");
        assertArrayEquals(array.toIntArray(), new int[]{1, 2, 3});
    }

    @Test
    public void parseModuleTabulated() {
        ConfigurationParser parser = new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module1.module"), fileReaderTabulated());
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule mod = parser.parseModule(new File("com/alesharik/webserver/configuration/config/lang/parser/module1.module"), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule module = mod;

        assertEquals("test", module.getName());
        ConfigurationTypedObject object = module.getModuleConfigurations().get(0);
        assertEquals("e", object.getName());
        assertEquals("e", object.getType());

        ConfigurationPrimitive primitive = (ConfigurationPrimitive) object.getElement("e");
        assertEquals("e", primitive.getName());
        assertEquals(1, ((ConfigurationPrimitive.Int) primitive).value());

        ConfigurationObject object1 = (ConfigurationObject) object.getElement("b");

        assertEquals("b", object1.getName());
        {
            ConfigurationPrimitive primitive1 = (ConfigurationPrimitive) object1.getElement("e");
            assertEquals("e", primitive1.getName());
            assertEquals(1, ((ConfigurationPrimitive.Int) primitive1).value());

            ConfigurationPrimitive primitive2 = (ConfigurationPrimitive) object1.getElement("b");
            assertPrimitiveEquals(primitive2, "b", 2);
        }

        ConfigurationObjectArray array = (ConfigurationObjectArray) object.getElement("c");
        assertArrayEquals(array.toIntArray(), new int[]{1, 2, 3});
    }

    @Test
    public void parseModuleWithNoDefineTabulated() {
        ConfigurationParser parser = new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module1WithNoDefine.module"), fileReaderTabulated());
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule mod = parser.parseModule(new File("com/alesharik/webserver/configuration/config/lang/parser/module1WithNoDefine.module"), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule module = mod;

        assertEquals("test", module.getName());
        ConfigurationTypedObject object = module.getModuleConfigurations().get(0);
        assertEquals("a", object.getName());
        assertEquals("a", object.getType());

        ConfigurationPrimitive primitive = (ConfigurationPrimitive) object.getElement("a");
        assertEquals("a", primitive.getName());
        assertEquals(1, ((ConfigurationPrimitive.Int) primitive).value());

        ConfigurationObject object1 = (ConfigurationObject) object.getElement("b");

        assertEquals("b", object1.getName());
        {
            ConfigurationPrimitive primitive1 = (ConfigurationPrimitive) object1.getElement("a");
            assertEquals("a", primitive1.getName());
            assertEquals(1, ((ConfigurationPrimitive.Int) primitive1).value());

            ConfigurationPrimitive primitive2 = (ConfigurationPrimitive) object1.getElement("b");
            assertPrimitiveEquals(primitive2, "b", 2);
        }

        ConfigurationObjectArray array = (ConfigurationObjectArray) object.getElement("c");
        assertArrayEquals(array.toIntArray(), new int[]{1, 2, 3});
    }

    @Test
    public void testUse() {
        ConfigurationParser parser = new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module2.module"), fileReaderTabulated());
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule mod = parser.parseModule(new File("com/alesharik/webserver/configuration/config/lang/parser/module2.module"), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule module = mod;

        assertEquals(6, module.getHelpers().size());

        {
            ExternalLanguageHelper one = module.getHelpers().get(0);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {
            ExternalLanguageHelper one = module.getHelpers().get(1);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test1.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {
            ExternalLanguageHelper one = module.getHelpers().get(2);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test2.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {//Parse default context
            ExternalLanguageHelper one = module.getHelpers().get(3);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.CALL, one.getContext());
        }
        {//Parse flip
            ExternalLanguageHelper one = module.getHelpers().get(4);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.CALL, one.getContext());
        }
        {//Parse global ctx
            ExternalLanguageHelper one = module.getHelpers().get(5);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.GLOBAL, one.getContext());
        }
    }

    @Test
    public void testUseWithNoDefine() {
        ConfigurationParser parser = new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module"), fileReaderTabulated());
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule mod = parser.parseModule(new File("com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module"), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
        //noinspection UnnecessaryLocalVariable idea bug
        ConfigurationModule module = mod;

        assertEquals(6, module.getHelpers().size());

        {
            ExternalLanguageHelper one = module.getHelpers().get(0);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {
            ExternalLanguageHelper one = module.getHelpers().get(1);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test1.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {
            ExternalLanguageHelper one = module.getHelpers().get(2);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test2.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.MODULE, one.getContext());
        }
        {//Parse default context
            ExternalLanguageHelper one = module.getHelpers().get(3);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.CALL, one.getContext());
        }
        {//Parse flip
            ExternalLanguageHelper one = module.getHelpers().get(4);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.CALL, one.getContext());
        }
        {//Parse global ctx
            ExternalLanguageHelper one = module.getHelpers().get(5);
            assertEquals("com/alesharik/webserver/configuration/config/lang/parser/test3.js", one.getHelperFile().toString());
            assertEquals("javascript", one.getLanguage());
            assertEquals(ExecutionContext.GLOBAL, one.getContext());
        }
    }

    @Test(expected = CodeParsingException.class)
    public void extractHelperWithNoLanguage() {
        module("com/alesharik/webserver/configuration/config/lang/parser/helpersWithNoLanguageName.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void extractHelperWithNoName() {
        module("com/alesharik/webserver/configuration/config/lang/parser/helpersWithNoName.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void extractHelperWithIllegalExecutionCtx() {
        module("com/alesharik/webserver/configuration/config/lang/parser/helpersWithIllegalCtx.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseEmptyModule() {
        module("com/alesharik/webserver/configuration/config/lang/parser/empty.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseUseWithNoFileExistence() {
        FileReader fileReader = fileReaderTabulated();
        when(fileReader.exists(any()))
                .thenReturn(false);
        module(fileReader, "com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseUseWithFolder() {
        FileReader fileReader = fileReaderTabulated();
        when(fileReader.isFile(any()))
                .thenAnswer(invocation -> !invocation.getArgument(0).toString().contains("test"));
        module(fileReader, "com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseUseWithInaccessibleFile() {
        FileReader fileReader = fileReaderTabulated();
        when(fileReader.canRead(any()))
                .thenReturn(false);
        module(fileReader, "com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseUseWithNotExecutableFile() {
        FileReader fileReader = fileReaderTabulated();
        when(fileReader.canExecute(any()))
                .thenReturn(false);
        module(fileReader, "com/alesharik/webserver/configuration/config/lang/parser/module2WithNoDefine.module");
        fail();
    }

    @Test
    public void parseAllKnown() {
        ConfigurationModule module = module("com/alesharik/webserver/configuration/config/lang/parser/all.module");
        ConfigurationObject object = module.getModuleConfigurations().get(0);

        assertEquals("a", object.getName());
        assertEquals("a", ((ConfigurationTypedObject) object).getType());

        assertPrimitiveEquals(object.getElement("a"), "a", 1);
        assertPrimitiveEquals(object.getElement("b"), "b", "test\n123");
        assertPrimitiveEquals(object.getElement("c"), "c", "asd,\n123");
        assertCodeEquals(object.getElement("d"), "d", "test", " asd\nasd\nqwe");
        assertArrEquals(object.getElement("e"), "e", new int[]{1, 2, 3});
        ConfigurationObject object1 = (ConfigurationObject) object.getElement("qwe");
        assertPrimitiveEquals(object1.getElement("a"), "a", 1);
        assertPrimitiveEquals(object1.getElement("b"), "b", 2);

        ConfigurationFunctionElement functionElement = (ConfigurationFunctionElement) object.getElement("asd");
        assertEquals("asd", functionElement.getName());
        assertEquals("calc", functionElement.getCodeInstruction());
    }

    @Test
    public void parseAllKnownVariant2() {
        ConfigurationModule module = module("com/alesharik/webserver/configuration/config/lang/parser/all2.module");
        ConfigurationObject object = module.getModuleConfigurations().get(0);

        assertEquals("a", object.getName());
        assertEquals("a", ((ConfigurationTypedObject) object).getType());

        assertPrimitiveEquals(object.getElement("a"), "a", 1);
        assertPrimitiveEquals(object.getElement("b"), "b", "\ntest\n123\n");
        assertPrimitiveEquals(object.getElement("c"), "c", "\nasd,\n123\n");
        assertCodeEquals(object.getElement("d"), "d", "test", "\nasd\nasd\nqwe");
        assertArrEquals(object.getElement("e"), "e", new int[]{1, 2, 3});
        ConfigurationObject object1 = (ConfigurationObject) object.getElement("qwe");
        assertPrimitiveEquals(object1.getElement("a"), "a", 1);
        assertPrimitiveEquals(object1.getElement("b"), "b", 2);

        ConfigurationFunctionElement functionElement = (ConfigurationFunctionElement) object.getElement("asd");
        assertEquals("asd", functionElement.getName());
        assertEquals("calc", functionElement.getCodeInstruction());
    }

    @Test
    public void parseAllKnownVariant3() {
        ConfigurationModule module = module("com/alesharik/webserver/configuration/config/lang/parser/all3.module");
        ConfigurationObject object = module.getModuleConfigurations().get(0);

        assertEquals("a", object.getName());
        assertEquals("a", ((ConfigurationTypedObject) object).getType());

        assertPrimitiveEquals(object.getElement("a"), "a", 1);
        assertPrimitiveEquals(object.getElement("b"), "b", "test123");
        assertPrimitiveEquals(object.getElement("c"), "c", "asd,123");
        assertCodeEquals(object.getElement("d"), "d", "test", "asdasdqwe");
        assertArrEquals(object.getElement("e"), "e", new int[]{1, 2, 3});
        ConfigurationObject object1 = (ConfigurationObject) object.getElement("qwe");
        assertPrimitiveEquals(object1.getElement("a"), "a", 1);
        assertPrimitiveEquals(object1.getElement("b"), "b", 2);

        ConfigurationFunctionElement functionElement = (ConfigurationFunctionElement) object.getElement("asd");
        assertEquals("asd", functionElement.getName());
        assertEquals("calc", functionElement.getCodeInstruction());
    }

    @Test
    public void parseAllKnownVariant4() {
        ConfigurationModule module = module("com/alesharik/webserver/configuration/config/lang/parser/all4.module");
        ConfigurationObject object = module.getModuleConfigurations().get(0);

        assertEquals("a", object.getName());
        assertEquals("a", ((ConfigurationTypedObject) object).getType());

        assertPrimitiveEquals(object.getElement("a"), "a", 1);
        assertPrimitiveEquals(object.getElement("b"), "b", "test123");
        assertPrimitiveEquals(object.getElement("c"), "c", "asd,123");
        assertCodeEquals(object.getElement("d"), "d", "test", "asdasd\nqwe");
        assertArrEquals(object.getElement("e"), "e", new int[]{1, 2, 3});
        ConfigurationObject object1 = (ConfigurationObject) object.getElement("qwe");
        assertPrimitiveEquals(object1.getElement("a"), "a", 1);
        assertPrimitiveEquals(object1.getElement("b"), "b", 2);

        ConfigurationFunctionElement functionElement = (ConfigurationFunctionElement) object.getElement("asd");
        assertEquals("asd", functionElement.getName());
        assertEquals("calc", functionElement.getCodeInstruction());
    }

    @Test
    public void parseSubModule() {
        ConfigurationModule module = module("com/alesharik/webserver/configuration/config/lang/parser/sub.module");
        ConfigurationObject object = module.getModuleConfigurations().get(0);

        assertEquals("a", object.getName());
        assertEquals("a", ((ConfigurationTypedObject) object).getType());

        assertPrimitiveEquals(object.getElement("a"), "a", 1);
        assertPrimitiveEquals(object.getElement("b"), "b", 1);
        assertPrimitiveEquals(object.getElement("c"), "c", 2);
    }

    @Test(expected = CodeParsingException.class)
    public void parseIllegalTest1() {
        module("com/alesharik/webserver/configuration/config/lang/parser/test1.module");
        fail();
    }

    @Test(expected = CodeParsingException.class)
    public void parseIllegalTest2() {
        module("com/alesharik/webserver/configuration/config/lang/parser/test2.module");
        fail();
    }

    @Test
    public void parseEndpoint() {
        ConfigurationEndpoint endpoint = endpoint("com/alesharik/webserver/configuration/config/lang/parser/a.endpoint");

        CustomEndpointSection section = endpoint.getCustomSection("test");
        assertNotNull(section);
        CustomEndpointSection.UseDirective directive = section.getUseDirectives().get(0);
        assertEquals("a", directive.getName());
        assertPrimitiveEquals(directive.getConfiguration().getElement("a"), "a", 1);
        assertPrimitiveEquals(directive.getConfiguration().getElement("b"), "b", 2);

        CustomEndpointSection.CustomProperty customProperty = directive.getCustomProperties().get(0);
        assertEquals("test", customProperty.getName());
        CustomEndpointSection.UseCommand command = customProperty.getUseCommands().get(0);
        assertEquals("qwerty", command.getReferent());
        assertEquals("with 123456", command.getArg());

        assertPrimitiveEquals(endpoint.getApiSection().getElement("a"), "a", "b");

        ScriptEndpointSection.ScriptSection scriptSection = endpoint.getScriptSection().getSection("start");
        ScriptEndpointSection.Command command1 = scriptSection.getCommands().get(0);
        assertEquals("start", command1.getName());
        assertPrimitiveEquals(command1.getArg(), "script", "a");
    }

    private ConfigurationEndpoint endpoint(String file) {
        ConfigurationParser parser = new ConfigurationParser(new File(file), fileReaderTabulated());
        return parser.parse();
    }

    private ConfigurationModule module(String file) {
        return parser().parseModule(new File(file), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
    }

    private ConfigurationModule module(FileReader fileReader, String file) {
        return parser(fileReader).parseModule(new File(file), ConfigParserInstructionsHeader.parse(Collections.emptyList(), null, mock(FileReader.class)));
    }

    private ConfigurationParser parser() {
        return new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module2.module"), fileReaderTabulated());
    }

    private ConfigurationParser parser(FileReader fileReader) {
        return new ConfigurationParser(new File("com/alesharik/webserver/configuration/config/lang/parser/module2.module"), fileReader);
    }

    private void assertPrimitiveEquals(ConfigurationElement element, String name, int primitive) {
        ConfigurationPrimitive primitive1 = (ConfigurationPrimitive) element;
        assertEquals(name, primitive1.getName());
        assertEquals(primitive, ((ConfigurationPrimitive.Int) primitive1).value());
    }

    private void assertPrimitiveEquals(ConfigurationElement element, String name, String primitive) {
        ConfigurationPrimitive primitive1 = (ConfigurationPrimitive) element;
        assertEquals(name, primitive1.getName());
        assertEquals(primitive, ((ConfigurationPrimitive.String) primitive1).value());
    }

    private void assertCodeEquals(ConfigurationElement element, String name, String lang, String code) {
        ConfigurationCodeElement primitive1 = (ConfigurationCodeElement) element;
        assertEquals(name, primitive1.getName());
        assertEquals(lang, primitive1.getLanguageName());
        assertEquals(code, primitive1.getCode());
    }

    private void assertArrEquals(ConfigurationElement array, String name, int[] arr) {
        ConfigurationObjectArray array1 = (ConfigurationObjectArray) array;
        assertEquals(name, array1.getName());
        assertArrayEquals(arr, array1.toIntArray());
    }

    private FileReader fileReader() {
        FileReader fileReader = mock(FileReader.class);
        when(fileReader.readFile(any()))
                .then(invocation -> {
                    InputStream stream = ConfigurationParserTest.class.getClassLoader().getResourceAsStream(invocation.getArgument(0).toString());
                    List<String> ret = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String l;
                    while((l = reader.readLine()) != null)
                        ret.add(l);
                    reader.close();
                    return ret;
                });
        when(fileReader.isFile(any()))
                .thenReturn(true);
        return fileReader;
    }


    private FileReader fileReaderTabulated() {
        FileReader fileReader = mock(FileReader.class);
        when(fileReader.readFile(any()))
                .then(invocation -> {
                    InputStream stream = ConfigurationParserTest.class.getClassLoader().getResourceAsStream(invocation.getArgument(0).toString());
                    List<String> ret = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String l;
                    while((l = reader.readLine()) != null)
                        ret.add(l.replaceAll(" {4}", "\t"));
                    reader.close();
                    return ret;
                });
        when(fileReader.isFile(any()))
                .thenReturn(true);
        when(fileReader.exists(any()))
                .thenReturn(true);
        when(fileReader.canExecute(any()))
                .thenReturn(true);
        when(fileReader.canRead(any()))
                .thenReturn(true);
        return fileReader;
    }

    interface A {

    }
}
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

import com.alesharik.webserver.configuration.config.lang.ConfigurationModule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationParserTest {
    @Test
    public void parseModule() throws IOException {
        FileReader fileReader = mock(FileReader.class);
        when(fileReader.readFile(any()))
                .then(invocation -> {
                    InputStream stream = ConfigurationParserTest.class.getClassLoader().getResourceAsStream("com/alesharik/webserver/configuration/config/lang/parser/module1.module");
                    List<String> ret = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String l;
                    while((l = reader.readLine()) != null)
                        ret.add(l);
                    reader.close();
                    return ret;
                });
        ConfigurationParser parser = new ConfigurationParser(File.createTempFile("alesharikwebserver", "testConfigurationParserTest0"), fileReader);
        ConfigurationModule module = parser.parseModule(File.createTempFile("alesharikwebserver", "testConfigurationParserTest1"), fileReader);
        System.out.println(module);
    }
}
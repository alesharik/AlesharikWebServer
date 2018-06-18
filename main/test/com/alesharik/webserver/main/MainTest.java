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

package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.config.lang.parser.ConfigurationParser;
import com.alesharik.webserver.configuration.config.lang.parser.FileReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {
    private static final TemporaryFolder temp = new TemporaryFolder();

    @BeforeClass
    public static void setup() throws IOException {
        temp.create();
        Main.coreModuleManager = new CoreModuleManagerImpl(temp.newFolder());
    }

    @AfterClass
    public static void teardown() {
        temp.delete();
    }

    @After
    public void tearDown() {
        System.getProperties().remove("config");
        System.getProperties().remove("core-modules-folder");
        System.getProperties().remove("parser");
    }

    @Test
    public void getConfigFileDefault() throws IOException {
        //no props
        File f = new File("main.endpoint");
        boolean e = f.exists();
        if(!e)
            assertTrue(f.createNewFile());
        assertTrue(f.setReadable(true));
        assertTrue(f.setExecutable(true));
        assertEquals(new File("main.endpoint"), Main.getConfigurationFile());
        if(!e)
            assertTrue(f.delete());
    }

    @Test
    public void getConfigFileProps() throws IOException {
        File file = temp.newFile();
        assertTrue(file.setReadable(true));
        assertTrue(file.setExecutable(true));
        System.getProperties().setProperty("config", file.getAbsolutePath());

        assertEquals(file, Main.getConfigurationFile());
    }

    @Test(expected = PropertyError.class)
    public void getConfigFileNotFound() {
        System.getProperties().setProperty("config", "/nope.asdf");
        Main.getConfigurationFile();
    }

    @Test(expected = PropertyError.class)
    public void getConfigFileIsFolder() throws IOException {
        System.getProperties().setProperty("config", temp.newFolder().getAbsolutePath());
        Main.getConfigurationFile();
    }

    @Test(expected = PropertyError.class)
    public void getConfigFileCantRead() throws IOException {
        File file = temp.newFile();
        assertTrue(file.setReadable(false));
        System.getProperties().setProperty("config", file.getAbsolutePath());
        Main.getConfigurationFile();
    }

    @Test(expected = PropertyError.class)
    public void getConfigFileCantExecute() throws IOException {
        File file = temp.newFile();
        assertTrue(file.setExecutable(false));
        assertTrue(file.setReadable(true));
        System.getProperties().setProperty("config", file.getAbsolutePath());
        Main.getConfigurationFile();
    }

    @Test
    public void getCoreModulesFolderDefault() {
        assertEquals(new File("core-modules/"), Main.getCoreModulesFolder());
    }

    @Test
    public void getCoreModulesFolderProps() throws IOException {
        File f = temp.newFolder();
        System.getProperties().setProperty("core-modules-folder", f.getAbsolutePath());
        assertEquals(f, Main.getCoreModulesFolder());
    }

    @Test(expected = PropertyError.class)
    public void getCoreModulesFolderFile() throws IOException {
        File f = temp.newFile();
        System.getProperties().setProperty("core-modules-folder", f.getAbsolutePath());
        Main.getCoreModulesFolder();
    }

    @Test
    public void getConfigParserDefault() throws IOException {
        assertEquals(ConfigurationParser.class, Main.getConfigurationParser(temp.newFile(), path -> Collections.emptyList()).getClass());
    }

    @Test
    public void getConfigParserProps() throws IOException {
        System.getProperties().setProperty("parser", "com.alesharik.webserver.main.MainTest$A");
        assertEquals(A.class, Main.getConfigurationParser(temp.newFile(), path -> Collections.emptyList()).getClass());
    }

    @Test(expected = PropertyError.class)
    public void getConfigParserNoClass() throws IOException {
        System.getProperties().setProperty("parser", "adsasd");
        Main.getConfigurationParser(temp.newFile(), path -> Collections.emptyList());
    }

    @Test(expected = PropertyError.class)
    public void getConfigParserIllegalConstructor() throws IOException {
        System.getProperties().setProperty("parser", "com.alesharik.webserver.main.MainTest$B");
        Main.getConfigurationParser(temp.newFile(), path -> Collections.emptyList());
    }

    @Test(expected = PropertyError.class)
    public void getConfigParserConstructorException() throws IOException {
        System.getProperties().setProperty("parser", "com.alesharik.webserver.main.MainTest$C");
        Main.getConfigurationParser(temp.newFile(), path -> Collections.emptyList());
    }

    public static final class C extends ConfigurationParser {

        public C(@Nonnull File endpoint, @Nonnull FileReader fileReader) {
            super(endpoint, fileReader);
            throw new IllegalArgumentException();
        }
    }

    public static final class B extends ConfigurationParser {

        public B(@Nonnull File endpoint, @Nonnull FileReader fileReader, @SuppressWarnings("unused") String s) {
            super(endpoint, fileReader);
        }
    }

    public static final class A extends ConfigurationParser {

        public A(@Nonnull File endpoint, @Nonnull FileReader fileReader) {
            super(endpoint, fileReader);
        }
    }
}
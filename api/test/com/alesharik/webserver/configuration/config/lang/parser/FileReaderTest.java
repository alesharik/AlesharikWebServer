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

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileReaderTest {
    private static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testExists() throws IOException {
        File a = temporaryFolder.newFile();
        FileReader fileReader = fileReader();

        assertTrue(fileReader.exists(a));
        assertFalse(fileReader.exists(new File("somerandomfile.qwerttyuio")));

        assertTrue(fileReader.exists(temporaryFolder.newFolder()));
        assertFalse(fileReader.exists(new File("./temp-wat/")));
    }

    @Test
    public void testIsFile() throws IOException {
        FileReader fileReader = fileReader();

        assertTrue(fileReader.isFile(temporaryFolder.newFile()));
        assertFalse(fileReader.isFile(temporaryFolder.newFolder()));
    }

    @Test
    public void testCanRead() throws IOException {
        File file = temporaryFolder.newFile();
        assertTrue(file.setReadable(true));

        assertTrue(fileReader().canRead(file));

        assertTrue(file.setReadable(false));

        assertFalse(fileReader().canRead(file));
    }

    @Test
    public void testCanExecute() throws IOException {
        File file = temporaryFolder.newFile();
        assertTrue(file.setExecutable(true));

        assertTrue(fileReader().canExecute(file));

        assertTrue(file.setExecutable(false));

        assertFalse(fileReader().canExecute(file));
    }

    private FileReader fileReader() {
        FileReader fileReader = mock(FileReader.class);
        when(fileReader.canExecute(any())).thenCallRealMethod();
        when(fileReader.canRead(any())).thenCallRealMethod();
        when(fileReader.isFile(any())).thenCallRealMethod();
        when(fileReader.exists(any())).thenCallRealMethod();
        return fileReader;
    }
}
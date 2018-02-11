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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigurationParser {
    protected final Path folder;
    protected final File endpoint;
    protected final FileReader fileReader;

    public ConfigurationParser(@Nonnull File endpoint, @Nonnull FileReader fileReader) {
        this.fileReader = fileReader;
        if(!endpoint.isFile())
            throw new IllegalArgumentException("Endpoint must be a file!");
        this.endpoint = endpoint;
        this.folder = endpoint.getParentFile().toPath();
    }

    public ConfigurationEndpoint parse() throws IOException {
        List<String> endpoint = Files.readAllLines(this.endpoint.toPath());
//        ConfigParserInstructionsHeader endpointHeader = ConfigParserInstructionsHeader.parse()
        return null;
    }
}

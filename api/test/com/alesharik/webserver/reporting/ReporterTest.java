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

package com.alesharik.webserver.reporting;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.io.File;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ReporterTest {
    private Reporter spy;

    @Before
    public void setUp() throws Exception {
        Reporter reporter = new TestReporter();
        spy = Mockito.spy(reporter);
    }

    @Test
    public void tickTest() throws Exception {
        spy.tick();

        verify(spy).report();
    }

    private static class TestReporter extends Reporter {

        @Override
        public void setup(@Nonnull File file, long tickPeriod, Element config) {

        }

        @Override
        public void reload(Element config) {

        }

        @Override
        protected void report() throws Exception {
        }

        @Override
        public void shutdown() {

        }

        @Override
        public void shutdownNow() {

        }

        @Override
        public String getName() {
            return "";
        }
    }
}
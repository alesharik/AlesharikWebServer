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

import com.alesharik.webserver.logger.Logger;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReportingModuleImplTest {
    private static final Reporter DUDE = new DudeReporter();
    private static final Reporter OK = new OkReporter();

    private ReportingModuleImpl reportingModule;

    @BeforeClass
    public static void init() throws Exception {
        Logger.setupLogger(File.createTempFile("AlesharikWebServer", "ReportingModuleImplTest"), 10);
    }

    @Before
    public void setUp() throws Exception {
        reportingModule = new ReportingModuleImpl();
        ReportingModuleImpl.listenReporter(DUDE.getClass());
        ReportingModuleImpl.listenReporter(OK.getClass());
    }

    @After
    public void tearDown() throws Exception {
        reportingModule.shutdown();
    }

    @Test
    public void tryReport() throws ParserConfigurationException, IOException, SAXException {
        File temp = File.createTempFile("AlesharikWebServer", "testFileForReportingModuleImplTest");
        String config = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread size. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>ok</name> <!-- reporter unique name !-->\n" +
                "            <file>" + temp.getAbsolutePath() + "</file> <!-- reporter file !-->\n" +
                "            <period>1000</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>true</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(config.getBytes(Charsets.UTF8_CHARSET)));
        reportingModule.parse(document.getDocumentElement());

        reportingModule.reportAll();

        String f = new String(Files.readAllBytes(temp.toPath()), Charsets.UTF8_CHARSET);
        assertTrue(Boolean.parseBoolean(f));
        temp.deleteOnExit();

        reportingModule.start();
    }

    @Test
    public void reloadTest() throws ParserConfigurationException, IOException, SAXException {
        File temp = File.createTempFile("AlesharikWebServer", "testFileForReportingModuleImplTest");
        String config = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread size. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>ok</name> <!-- reporter unique name !-->\n" +
                "            <file>" + temp.getAbsolutePath() + "</file> <!-- reporter file !-->\n" +
                "            <period>1000</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>true</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(config.getBytes(Charsets.UTF8_CHARSET)));
        reportingModule.parse(document.getDocumentElement());

        reportingModule.reportAll();

        String f = new String(Files.readAllBytes(temp.toPath()), Charsets.UTF8_CHARSET);
        assertTrue(Boolean.parseBoolean(f));
        temp.deleteOnExit();
        temp = File.createTempFile("AlesharikWebServer", "testFileForReportingModuleImplTest");
        temp.deleteOnExit();

        reportingModule.start();

        String config1 = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread size. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>ok</name> <!-- reporter unique name !-->\n" +
                "            <file>" + temp.getAbsolutePath() + "</file> <!-- reporter file !-->\n" +
                "            <period>500</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>false</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder1 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document1 = documentBuilder1.parse(new ByteArrayInputStream(config1.getBytes(Charsets.UTF8_CHARSET)));
        reportingModule.reload(document1.getDocumentElement());
        reportingModule.reportAll();

        String f1 = new String(Files.readAllBytes(temp.toPath()), Charsets.UTF8_CHARSET);
        assertFalse(Boolean.parseBoolean(f1));
    }

    @Test
    public void tryGetNotExistingReporter() throws ParserConfigurationException, IOException, SAXException, InterruptedException, ExecutionException {
        File temp = File.createTempFile("AlesharikWebServer", "testFileForReportingModuleImplTest");

        String config = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread size. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>none</name> <!-- reporter unique name !-->\n" +
                "            <file>" + temp + "</file> <!-- reporter file !-->\n" +
                "            <period>1000</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>true</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(config.getBytes(Charsets.UTF8_CHARSET)));
        reportingModule.parse(document.getDocumentElement());

        reportingModule.reportAll();
    }

    @Test
    public void testListenClass() throws Exception {
        PrintStream serr = mock(PrintStream.class);
        System.setErr(serr);

        ReportingModuleImpl.listenReporter(Rep1.class);
        verify(serr).println("Class " + Rep1.class.getCanonicalName() + " doesn't have ReporterName annotation and will be ignored!");

        ReportingModuleImpl.listenReporter(Rep2.class);
        verify(serr).println("Class " + Rep2.class.getCanonicalName() + " doesn't have empty constructor and will be ignored!");

        ReportingModuleImpl.listenReporter(Rep3.class);
        assertTrue(ReportingModuleImpl.reporters.containsKey("test1"));
    }

    @Test
    public void testAddReporter() throws Exception {
        Reporter reporter = mock(Reporter.class);
        Reporter reporter1 = mock(Reporter.class);
        reportingModule.enableReporter(reporter);
        reportingModule.start();
        reportingModule.enableReporter(reporter1);
        reportingModule.reportAll();
        reportingModule.disableReporter(reporter);
        reportingModule.reportAll();
        reportingModule.shutdownNow();

        verify(reporter, atLeast(1)).tick();
        verify(reporter, atLeast(2)).tick();
    }

    @Test
    public void getNotExistingReporter() throws Exception {
        assertNull(ReportingModuleImpl.getReporter("not_existing"));
    }

    @Test
    public void getErrorReporter() throws Exception {
        ReportingModuleImpl.listenReporter(RepError.class);
        assertNull(ReportingModuleImpl.getReporter("error"));
    }

    @Test
    public void getReporterTest() throws Exception {
        assertNotNull(ReportingModuleImpl.getReporter("dude"));
    }

    @ReporterName("error")
    private static final class RepError extends Reporter {

        public RepError() {
            throw new IllegalArgumentException("Nope");
        }

        @Override
        public void setup(@Nonnull File file, long tickPeriod, @Nullable Element config) {

        }

        @Override
        public void reload(@Nullable Element config) {

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

        @Nonnull
        @Override
        public String getName() {
            return "";
        }
    }

    private static final class Rep1 extends Reporter {

        @Override
        public void setup(@Nonnull File file, long tickPeriod, @Nullable Element config) {

        }

        @Override
        public void reload(@Nullable Element config) {

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

        @Nonnull
        @Override
        public String getName() {
            return "";
        }
    }

    @ReporterName("test")
    private static final class Rep2 extends Reporter {

        public Rep2(int i) {
        }

        @Override
        public void setup(@Nonnull File file, long tickPeriod, @Nullable Element config) {

        }

        @Override
        public void reload(@Nullable Element config) {

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

        @Nonnull
        @Override
        public String getName() {
            return null;
        }
    }

    @ReporterName("test1")
    private static final class Rep3 extends Reporter {

        @Override
        public void setup(@Nonnull File file, long tickPeriod, @Nullable Element config) {

        }

        @Override
        public void reload(@Nullable Element config) {

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

        @Nonnull
        @Override
        public String getName() {
            return null;
        }
    }

    @ReporterName("dude")
    private static class DudeReporter extends Reporter {
        @Override
        public void setup(@Nullable File file, long tickPeriod, Element config) {

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
            return "dude";
        }
    }

    @ReporterName("ok")
    private static class OkReporter extends Reporter {
        private File file;
        private AtomicBoolean configOK = new AtomicBoolean(false);

        @Override
        public void setup(@Nullable File file, long tickPeriod, Element config) {
            this.file = file;
            Element ok = (Element) config.getElementsByTagName("ok").item(0);
            if(ok != null && Boolean.parseBoolean(ok.getTextContent())) {
                configOK.set(true);
            }
        }

        @Override
        public void reload(Element config) {
            Element ok = (Element) config.getElementsByTagName("ok").item(0);
            if(ok != null && Boolean.getBoolean(ok.getTextContent())) {
                configOK.set(true);
            } else
                configOK.set(false);
        }

        @Override
        protected void report() throws Exception {
            Files.write(file.toPath(), String.valueOf(configOK.get()).getBytes(Charsets.UTF8_CHARSET), StandardOpenOption.TRUNCATE_EXISTING);
        }

        @Override
        public void shutdown() {

        }

        @Override
        public void shutdownNow() {

        }

        @Override
        public String getName() {
            return "ok";
        }
    }
}
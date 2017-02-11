package com.alesharik.webserver.reporter;

import com.alesharik.webserver.api.ConcurrentCompletableFuture;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.reporting.Reporter;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class ReportingModuleImplTest {
    private static final Reporter DUDE = new Reporter() {
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
    };

    private static final Reporter OK = new Reporter() {
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
            }
        }

        @Override
        protected void report() throws Exception {
            Files.write(file.toPath(), String.valueOf(configOK.get()).getBytes(Charsets.UTF8_CHARSET));
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
    };
    private ReportingModuleImpl reportingModule;

    @Before
    public void setUp() throws Exception {
        reportingModule = new ReportingModuleImpl(Thread.currentThread().getThreadGroup());
        reportingModule.registerNewReporter(DUDE);
        reportingModule.registerNewReporter(OK);

        Logger.setupLogger(File.createTempFile("sdsfsfdsdfd", "asddsfdfssdfsdf"), 100);
    }

    @After
    public void tearDown() throws Exception {
        reportingModule.shutdown();
    }

    @Test
    public void tryReport() throws ParserConfigurationException, IOException, SAXException {
        String config = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread count. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>ok</name> <!-- reporter unique name !-->\n" +
                "            <file>./test.csv</file> <!-- reporter file !-->\n" +
                "            <period>1000</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>true</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(config.getBytes()));
        reportingModule.parse(document.getDocumentElement());

        reportingModule.reportAll();

        File file = new File("./test.csv");
        String f = new String(Files.readAllBytes(file.toPath()), Charsets.UTF8_CHARSET);
        assertTrue(Boolean.parseBoolean(f));
        file.deleteOnExit();

        reportingModule.start();
    }

    @Test
    public void tryGetNotExistingReporter() throws ParserConfigurationException, IOException, SAXException, InterruptedException, ExecutionException {
        ConcurrentCompletableFuture<Boolean> isOk = new ConcurrentCompletableFuture<>();

        Logger.addListener((prefixes, message) -> {
            if(prefixes.equals("[ReportingModule]")) {
                if(message.equals("Reporter none not found! Skipping...")) {
                    isOk.set(true);
                } else {
                    isOk.set(false);
                }
            }
        });

        String config = "<configuration>\n" +
                "    <threadCount>10</threadCount> <!-- ticking pool thread count. Default is 10 !-->\n" +
                "    <reporters> <!-- reporters to execute !-->\n" +
                "        <reporter>\n" +
                "            <name>none</name> <!-- reporter unique name !-->\n" +
                "            <file>./test.csv</file> <!-- reporter file !-->\n" +
                "            <period>1000</period> <!-- reporter call period in milliseconds !-->\n" +
                "            <configuration> <!-- reporter configuration !-->\n" +
                "                <ok>true</ok>\n" +
                "            </configuration>\n" +
                "        </reporter>\n" +
                "    </reporters>\n" +
                "</configuration>";
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(config.getBytes()));
        reportingModule.parse(document.getDocumentElement());

        assertTrue(isOk.get());

        reportingModule.reportAll();
    }

}
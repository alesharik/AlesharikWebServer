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

package com.alesharik.webserver.configuration.module.layer.meta;

import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.exception.DevError;
import com.alesharik.webserver.configuration.module.Shutdown;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.Layer;
import com.alesharik.webserver.configuration.module.layer.SubModule;
import com.alesharik.webserver.configuration.module.meta.MetaInvokeException;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

import static com.alesharik.webserver.test.TestUtils.assertUtilityClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LayerMetaFactoryTest {
    private static BiConsumer<LayerAdapter, Object> mock;

    @Before
    public void setUp() throws Exception {
        //noinspection unchecked
        mock = mock(BiConsumer.class);
        LayerMetaFactory.listenClass(ProcMock.class);
    }

    @Test
    public void utility() {
        assertUtilityClass(LayerMetaFactory.class);
    }

    @Test
    public void create() {
        Layer1 layer1 = new Layer1();

        LayerAdapter adapter = LayerMetaFactory.create(layer1);
        verify(mock, times(1)).accept(adapter, layer1);

        assertFalse(adapter.isRunning());
        adapter.start();
        assertTrue(adapter.isRunning());
        adapter.shutdownNow();
        assertFalse(adapter.isRunning());
    }

    @Test
    public void createWithProcessorError() {
        doThrow(new IllegalMonitorStateException()).when(mock).accept(any(), any());

        Layer1 layer1 = new Layer1();

        LayerAdapter adapter = LayerMetaFactory.create(layer1);
        verify(mock, times(1)).accept(adapter, layer1);

        assertFalse(adapter.isRunning());
        adapter.start();
        assertTrue(adapter.isRunning());
        adapter.shutdownNow();
        assertFalse(adapter.isRunning());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createIllegal() {
        LayerMetaFactory.create(new Object());
        fail();
    }

    @Test
    public void addBeanProcessor() {
        assertEquals(1, LayerMetaFactory.processors.size());

        LayerMetaFactory.listenClass(BeanProcessor.class);
        assertEquals(2, LayerMetaFactory.processors.size());

        LayerMetaFactory.listenClass(BeanProcessor.class);
        assertEquals(2, LayerMetaFactory.processors.size());

        LayerMetaFactory.listenClass(ProcMock.class);
        assertEquals(2, LayerMetaFactory.processors.size());
    }

    @Test
    public void logic() {
        Layer1 layer = new Layer1();

        LayerAdapter adapter = LayerMetaFactory.create(layer);
        assertFalse(adapter.isRunning());

        adapter.start();

        verify(layer.mock, times(1)).start();
        verify(layer.layer2.mock, times(2)).start();
        verify(layer.layer2.subModule.mock, times(1)).start();
        verify(layer.subModule.mock, times(1)).start();
        verify(layer.subModule1.mock, times(1)).start();
        assertTrue(adapter.isRunning());
        for(LayerAdapter layerAdapter : adapter.getSubLayers()) {
            assertTrue(layerAdapter.isRunning());
            for(LayerAdapter adapter1 : layerAdapter.getSubLayers())
                assertTrue(adapter1.isRunning());
        }
        for(SubModuleAdapter subModuleAdapter : adapter.getSubModules())
            assertTrue(subModuleAdapter.isRunning());

        adapter.shutdown();

        verify(layer.mock, times(1)).shutdown();
        verify(layer.layer2.mock, times(1)).shutdown();
        verify(layer.layer2.subModule.mock, times(1)).shutdown();
        verify(layer.subModule.mock, times(1)).shutdown();
        verify(layer.subModule1.mock, times(1)).shutdown();
        assertFalse(adapter.isRunning());
        for(LayerAdapter layerAdapter : adapter.getSubLayers()) {
            assertFalse(layerAdapter.isRunning());
            for(LayerAdapter adapter1 : layerAdapter.getSubLayers())
                assertFalse(adapter1.isRunning());
        }
        for(SubModuleAdapter subModuleAdapter : adapter.getSubModules())
            assertFalse(subModuleAdapter.isRunning());

        adapter.start();

        verify(layer.mock, times(2)).start();
        verify(layer.layer2.mock, times(4)).start();
        verify(layer.layer2.subModule.mock, times(2)).start();
        verify(layer.subModule.mock, times(2)).start();
        verify(layer.subModule1.mock, times(2)).start();
        assertTrue(adapter.isRunning());
        for(LayerAdapter layerAdapter : adapter.getSubLayers()) {
            assertTrue(layerAdapter.isRunning());
            for(LayerAdapter adapter1 : layerAdapter.getSubLayers())
                assertTrue(adapter1.isRunning());
        }
        for(SubModuleAdapter subModuleAdapter : adapter.getSubModules())
            assertTrue(subModuleAdapter.isRunning());

        adapter.shutdownNow();

        verify(layer.mock, times(1)).shutdownNow();
        verify(layer.layer2.mock, times(1)).shutdownNow();
        verify(layer.layer2.subModule.mock, times(1)).shutdownNow();
        verify(layer.subModule.mock, times(1)).shutdownNow();
        verify(layer.subModule1.mock, times(1)).shutdownNow();
        assertFalse(adapter.isRunning());
        for(LayerAdapter layerAdapter : adapter.getSubLayers()) {
            assertFalse(layerAdapter.isRunning());
            for(LayerAdapter adapter1 : layerAdapter.getSubLayers())
                assertFalse(adapter1.isRunning());
        }
        for(SubModuleAdapter subModuleAdapter : adapter.getSubModules())
            assertFalse(subModuleAdapter.isRunning());
    }

    @Test
    public void parseOK() {
        Layer1 layer = new Layer1();

        LayerAdapter adapter = LayerMetaFactory.create(layer);
        assertEquals("test", adapter.getName());
        assertNotNull(adapter.getCustomData());

        assertEquals(1, adapter.getSubLayers().size());
        LayerAdapter subLayer = adapter.getSubLayers().get(0);
        assertEquals("test1", subLayer.getName());

        {
            assertEquals(1, subLayer.getSubModules().size());
            assertTrue(subLayer.getSubLayers().isEmpty());
            SubModuleAdapter subModuleAdapter = subLayer.getSubModules().get(0);
            assertEquals("mod", subModuleAdapter.getName());
        }

        assertEquals(2, adapter.getSubModules().size());

        SubModuleAdapter subModuleAdapter = adapter.getSubModules().get(0);
        assertEquals("mod", subModuleAdapter.getName());

        SubModuleAdapter subModuleAdapter1 = adapter.getSubModules().get(1);
        assertEquals("mod", subModuleAdapter1.getName());
    }

    @Test(expected = DevError.class)
    public void executionError() {
        Layer1 layer = new Layer1();
        doThrow(new DevError("a", "a", "a")).when(layer.layer2.subModule.mock).start();
        LayerMetaFactory.create(layer).start();
        fail();
    }

    @Test(expected = MetaInvokeException.class)
    public void executionException() {
        Layer1 layer = new Layer1();
        doThrow(new IllegalMonitorStateException()).when(layer.layer2.subModule.mock).start();
        LayerMetaFactory.create(layer).start();
        fail();
    }

    @Test
    public void moduleWithNoAutoInvoke() {
        ManualModule manualModule = new ManualModule();

        LayerAdapter adapter = LayerMetaFactory.create(manualModule);
        adapter.start();

        verify(manualModule.subModule.mock, never()).start();

        assertFalse(adapter.isRunning());
    }

    @Layer(value = "asd", autoInvoke = false)
    private static final class ManualModule {
        @Getter
        private final LayerAdapter mock = mock(LayerAdapter.class);
        private final SubModuleImpl subModule = new SubModuleImpl();

        @Start
        void start() {
            mock.start();
        }

        @Shutdown
        void shutdown() {
            mock.shutdown();
        }

        @ShutdownNow
        void shutdownNow() {
            mock.shutdownNow();
        }
    }

    @Bean
    private static final class BeanProcessor implements LayerProcessor {

        @Override
        public void processLayer(@Nonnull LayerAdapter layerAdapter, @Nonnull Object layer) {

        }
    }

    @Layer("test")
    private static final class Layer1 {
        @Getter
        private final LayerAdapter mock = mock(LayerAdapter.class);
        private final Layer2 layer2 = new Layer2();
        private final SubModuleImpl subModule = new SubModuleImpl();
        private final SubModuleImpl subModule1 = new SubModuleImpl();
        private transient final SubModuleImpl tans = new SubModuleImpl();

        @Start
        void start() {
            mock.start();
        }

        @Shutdown
        void shutdown() {
            mock.shutdown();
        }

        @ShutdownNow
        void shutdownNow() {
            mock.shutdownNow();
        }
    }

    @Layer("test1")
    private static final class Layer2 {
        @Getter
        private final LayerAdapter mock = mock(LayerAdapter.class);
        private final SubModuleImpl subModule = new SubModuleImpl();

        @Start
        void start() {
            mock.start();
        }

        @Start
        void start1() {
            mock.start();
        }

        @Shutdown
        void shutdown() {
            mock.shutdown();
        }

        @ShutdownNow
        void shutdownNow() {
            mock.shutdownNow();
        }
    }

    @SubModule("mod")
    private static final class SubModuleImpl {
        @Getter
        private final SubModuleAdapter mock = mock(SubModuleAdapter.class);

        @Start
        void start() {
            mock.start();
        }

        @Shutdown
        void shutdown() {
            mock.shutdown();
        }

        @ShutdownNow
        void shutdownNow() {
            mock.shutdownNow();
        }
    }

    private static final class ProcMock implements LayerProcessor {

        @Override
        public void processLayer(@Nonnull LayerAdapter layerAdapter, @Nonnull Object layer) {
            mock.accept(layerAdapter, layer);
        }
    }
}
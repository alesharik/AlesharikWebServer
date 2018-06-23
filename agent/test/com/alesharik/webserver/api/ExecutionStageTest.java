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

package com.alesharik.webserver.api;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

@ExecutionStage.AuthorizedImpl
public class ExecutionStageTest {
    @Before
    public void setup() {
        ExecutionStage.setState(ExecutionStage.NOT_STARTED);
    }

    @Test
    public void getStage() {
        assertEquals(ExecutionStage.NOT_STARTED, ExecutionStage.getCurrentStage());
    }

    @Test
    public void setStageCorrect() {
        ExecutionStage.setState(ExecutionStage.CONFIG);
        assertEquals(ExecutionStage.CONFIG, ExecutionStage.getCurrentStage());
    }

    @Test(expected = SecurityException.class)
    public void setStageWrongClass() {
        C.set();
        fail();
    }

    @Test
    public void enabled() {
        assertFalse(ExecutionStage.isEnabled());
        ExecutionStage.enable();
        assertTrue(ExecutionStage.isEnabled());
        ExecutionStage.enable();
        assertTrue(ExecutionStage.isEnabled());
    }

    @Test
    public void valid() {
        ExecutionStage.setState(ExecutionStage.CONFIG);
        assertTrue(ExecutionStage.valid(new ExecutionStage[]{ExecutionStage.AGENT, ExecutionStage.CONFIG, ExecutionStage.CORE_MODULES}));
        assertFalse(ExecutionStage.valid(new ExecutionStage[]{ExecutionStage.CORE_MODULES}));
    }

    @Test
    public void callingClass() {
        assertEquals(C.class, C.get());
    }

    private static final class C {
        private static void set() {
            ExecutionStage.setState(ExecutionStage.AGENT);
        }

        private static Class<?> get() {
            return ExecutionStage.CallerClass.INSTANCE.getCaller();
        }
    }
}
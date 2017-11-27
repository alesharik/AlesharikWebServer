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

package com.alesharik.webserver.exception.error;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class UnexpectedBehaviorErrorTest {
    @Test
    public void ctorWithMessage() throws Exception {
        UnexpectedBehaviorError error = new UnexpectedBehaviorError("test");
        assertEquals("test", error.getMessage());
    }

    @Test
    public void ctorWithCause() throws Exception {
        Exception cause = new RuntimeException();
        UnexpectedBehaviorError error = new UnexpectedBehaviorError(cause);
        assertSame(cause, error.getCause());
    }

    @Test
    public void ctorWithCauseAndMessage() throws Exception {
        Exception cause = new RuntimeException();
        UnexpectedBehaviorError error = new UnexpectedBehaviorError("test", cause);
        assertSame(cause, error.getCause());
        assertEquals("test", error.getMessage());
    }
}
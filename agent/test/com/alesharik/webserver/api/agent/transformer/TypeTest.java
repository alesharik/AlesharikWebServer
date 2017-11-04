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

package com.alesharik.webserver.api.agent.transformer;

import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import java.security.ProtectionDomain;

import static com.alesharik.webserver.api.agent.transformer.Param.Type.*;
import static org.junit.Assert.assertEquals;

public class TypeTest {
    @Test
    public void testParse() throws Exception {
        Param.Type[] types = parse(this.getClass().getDeclaredMethod("testMethod", String.class, String.class, ClassLoader.class, byte[].class, ClassNode.class, Class.class, ProtectionDomain.class, String.class));
        assertEquals(NULL, types[0]);
        assertEquals(CLASS_NAME, types[1]);
        assertEquals(CLASS_LOADER, types[2]);
        assertEquals(CLASSFILE_BUFFER, types[3]);
        assertEquals(CLASS_NODE, types[4]);
        assertEquals(CLASS_BEING_REDEFINED, types[5]);
        assertEquals(PROTECTION_DOMAIN, types[6]);
        assertEquals(NULL, types[7]);
    }

    @Test
    public void testParseIncorrect() throws Exception {
        Param.Type[] types = parse(this.getClass().getDeclaredMethod("testMethodIncorrect", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
        for(int i = 0; i < 8; i++) {
            assertEquals(NULL, types[i]);
        }
    }

    @SuppressWarnings("unused")
    private void testMethod(@Param(NULL) String a, @Param(CLASS_NAME) String b, @Param(CLASS_LOADER) ClassLoader c, @Param(CLASSFILE_BUFFER) byte[] data, @Param(CLASS_NODE) ClassNode classNode, @Param(CLASS_BEING_REDEFINED) Class<?> clazz, @Param(PROTECTION_DOMAIN) ProtectionDomain protectionDomain, String asd) {

    }


    @SuppressWarnings("unused")
    private void testMethodIncorrect(@Param(NULL) Object a, @Param(CLASS_NAME) Object b, @Param(CLASS_LOADER) Object c, @Param(CLASSFILE_BUFFER) Object data, @Param(CLASS_NODE) Object classNode, @Param(CLASS_BEING_REDEFINED) Object clazz, @Param(PROTECTION_DOMAIN) Object protectionDomain, Object asd) {

    }
}
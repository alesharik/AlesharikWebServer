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

package com.alesharik.webserver.api.utils.classloader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.objectweb.asm.Opcodes.*;

public class GeneratedClassLoaderTest {
    private static Mock mock;

    private GeneratedClassLoader classLoader;
    private byte[] classData;

    @Before
    public void setUp() throws Exception {
        classLoader = new GeneratedClassLoader();

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(52, ACC_PUBLIC + ACC_SUPER, "Test", null, Type.getInternalName(Object.class), null);

        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "test", "()V", null, null);
        visitor.visitCode();
        visitor.visitMethodInsn(INVOKESTATIC, Type.getInternalName(GeneratedClassLoaderTest.class), "mockExec", "()V", false);
        visitor.visitInsn(RETURN);
        visitor.visitEnd();

        classWriter.visitEnd();
        classData = classWriter.toByteArray();

        mock = Mockito.mock(Mock.class);
    }

    public static void mockExec() {
        mock.mock();
    }

    @Test
    public void testCycle() throws Exception {
        assertEquals(0, classLoader.getGeneratedClassCount());

        classLoader.addGeneratedClass("Test", classData);
        Class<?> clazz = classLoader.loadClass("Test");
        Method method = clazz.getDeclaredMethod("test");
        method.invoke(clazz.newInstance());

        Mockito.verify(mock, Mockito.times(1)).mock();

        assertEquals(1, classLoader.getGeneratedClassCount());
    }

    @Test(expected = ClassAlreadyExistsException.class)
    public void testRegisterSameClass() throws Exception {
        classLoader.addGeneratedClass("Test", classData);
        classLoader.addGeneratedClass("Test", classData);
    }

    private interface Mock {
        void mock();
    }
}
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

package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.IllegalClassFormatException;

@ClassTransformer
public final class SharedStorageClassTransformer {

    @TransformAll
    public static byte[] transform(@Param(Param.Type.CLASS_NAME) String className, @Param(Param.Type.CLASSFILE_BUFFER) byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classfileBuffer);
            classReader.accept(classNode, 0);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            classNode.accept(new SharedStorageClassVisitor(classWriter));
            return classWriter.toByteArray();
        } catch (Exception e) {
            if(e.getCause() instanceof ClassNotFoundException) {
                return classfileBuffer;
            }
            System.err.println("Problem in class " + className);
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}

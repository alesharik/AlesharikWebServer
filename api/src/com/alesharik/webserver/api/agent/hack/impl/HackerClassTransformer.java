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

package com.alesharik.webserver.api.agent.hack.impl;

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.api.agent.hack.Extends;
import com.alesharik.webserver.api.agent.hack.Hacker;
import com.alesharik.webserver.api.agent.hack.LoadFrom;
import com.alesharik.webserver.api.agent.hack.StoreTo;
import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import com.alesharik.webserver.api.documentation.PrivateApi;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alesharik.webserver.api.agent.ASMUtils.*;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ASM5;

//TODO logging
//TODO constructor handling
@PrivateApi
@UtilityClass
@ClassTransformer
class HackerClassTransformer {
    private static final boolean ENABLED;

    private static final Type HACKER_ANNOTATION_TYPE = Type.getType(Hacker.class);
    private static final Type EXTENDS_ANNOTATION_TYPE = Type.getType(Extends.class);
    private static final Type MAGIC_ACCESSOR_IMPL = Type.getType("sun/reflect/MagicAccessorImpl");
    private static final List<String> extendsClasses = new ArrayList<>();

    static {
        ENABLED = Agent.isClassRetransformationSupported();
        if(!ENABLED)
            System.out.println("Class retransformation not supported! Hackers will not work!");
    }

    @TransformAll
    public static byte[] transform(@Param(Param.Type.CLASSFILE_BUFFER) byte[] data, @Param(Param.Type.CLASS_NAME) String name, @Param(Param.Type.CLASS_NODE) ClassNode classNode, @Param(Param.Type.CLASS_LOADER) ClassLoader classLoader) {
        if(!ENABLED)
            return null;

        boolean changed = false;
        if(extendsClasses.contains(name)) {
            changed = true;
            data = ensureCanExtends(data, classNode);
        }

        if(!hasAnnotation(classNode, HACKER_ANNOTATION_TYPE))
            return changed ? data : null;

        Pair<String, Object>[] extend = getAnnotationParams(classNode, EXTENDS_ANNOTATION_TYPE);
        String extendName = "";
        if(extend != null) {
            extendName = "";
            for(Pair<String, Object> stringObjectPair : extend) {
                if("value".equals(stringObjectPair.getKey()))
                    extendName = stringObjectPair.getValue().toString();
            }
            if(!extendName.isEmpty()) {
                try {
                    Class<?> clazz = Class.forName(extendName, false, classLoader);
                    String internalName = Type.getInternalName(clazz);
                    if(!internalName.equals(MAGIC_ACCESSOR_IMPL.getInternalName()))
                        extendsClasses.add(internalName);
                    Agent.retransform(clazz);
                } catch (ClassNotFoundException e) {
                    System.out.println("Hacker " + name + " extends annotation will be ignored: class " + extendName + " not found!");
                    extendName = "";
                } catch (UnmodifiableClassException e) {
                    System.out.println("Hacker " + name + " extends annotation will be ignored: class " + extendName + " not is unmodifiable!");
                    extendName = "";
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(new ClassReader(data), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitorImpl classVisitor = new ClassVisitorImpl(classWriter, extendName);
        classNode.accept(classVisitor);
        return classWriter.toByteArray();
    }

    private static byte[] ensureCanExtends(byte[] clazz, ClassNode classNode) {
        ClassWriter classWriter = new ClassWriter(new ClassReader(clazz), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        PrepareExtendsClassVisitor classVisitor = new PrepareExtendsClassVisitor(classWriter);
        classNode.accept(classVisitor);
        return classWriter.toByteArray();
    }

    private static final class PrepareExtendsClassVisitor extends ClassVisitor {
        public PrepareExtendsClassVisitor(ClassVisitor cv) {
            super(ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if(Modifier.isPrivate(access) || access == 0)
                access = ACC_PROTECTED;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(Modifier.isPrivate(access) || access == 0)
                access = ACC_PROTECTED;
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if(Modifier.isPrivate(access) || access == 0)
                access = ACC_PROTECTED;
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static final class ClassVisitorImpl extends ClassVisitor {
        private final String extend;

        public ClassVisitorImpl(ClassVisitor cv, String extend) {
            super(ASM5, cv);
            this.extend = extend;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if(!extend.isEmpty())
                superName = extend;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitorImpl(super.visitMethod(access, name, desc, signature, exceptions));
        }

        private static final class MethodVisitorImpl extends MethodVisitor {
            private static final String LOAD_FROM_DESCRIPTOR = Type.getDescriptor(LoadFrom.class);
            private static final String STORE_TO_DESCRIPTOR = Type.getDescriptor(StoreTo.class);

            private final Map<Integer, FieldAccess> annotations = new HashMap<>();

            public MethodVisitorImpl(MethodVisitor mv) {
                super(ASM5, mv);
            }

            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
                FieldAccess access = null;
                for(int anIndex : index)
                    if(annotations.containsKey(anIndex))
                        access = annotations.get(anIndex);
                if(access == null)
                    access = new FieldAccess();
                for(int i : index)
                    annotations.put(i, access);

                if(LOAD_FROM_DESCRIPTOR.equals(desc))
                    return new ReadAnnotationVisitorImpl(super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible), access);
                else if(STORE_TO_DESCRIPTOR.equals(desc))
                    return new WriteAnnotationVisitorImpl(super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible), access);
                else
                    return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            }

            @Override
            public void visitVarInsn(int opcode, int var) {
                if(annotations.containsKey(var)) {
                    FieldAccess access = annotations.get(var);
                    if(access.canWrite() && isStore(opcode)) {
                        visitFieldInsn(access.isWriteStatic() ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD, access.getWriteOwner(), access.getWrite(), access.getWriteDescriptor());
                        return;
                    } else if(access.canRead() && isLoad(opcode)) {
                        visitFieldInsn(access.isWriteStatic() ? Opcodes.GETSTATIC : Opcodes.GETFIELD, access.getWriteOwner(), access.getWrite(), access.getWriteDescriptor());
                        return;
                    }
                }
                super.visitVarInsn(opcode, var);
            }

            @Getter
            @Setter
            @EqualsAndHashCode
            @ToString
            private static final class FieldAccess {
                private String read;
                private String write;

                private String readOwner;
                private String writeOwner;

                private String readDescriptor;
                private String writeDescriptor;

                private boolean readStatic;
                private boolean writeStatic;

                public boolean canRead() {
                    return !read.isEmpty() && !readOwner.isEmpty() && !readDescriptor.isEmpty();
                }

                public boolean canWrite() {
                    return !write.isEmpty() && !writeOwner.isEmpty() && !writeDescriptor.isEmpty();
                }
            }

            private static final class ReadAnnotationVisitorImpl extends AnnotationVisitor {
                private final FieldAccess fieldAccess;

                public ReadAnnotationVisitorImpl(AnnotationVisitor visitor, FieldAccess fieldAccess) {
                    super(ASM5, visitor);
                    this.fieldAccess = fieldAccess;
                }

                @Override
                public void visit(String name, Object value) {
                    if("value".equals(name))
                        fieldAccess.setRead(value.toString());
                    else if("owner".equals(name))
                        fieldAccess.setReadOwner(value.toString());
                    else if("descriptor".equals(name))
                        fieldAccess.setReadDescriptor(value.toString());
                    else if("isStatic".equals(name))
                        fieldAccess.setReadStatic((Boolean) value);
                    super.visit(name, value);
                }
            }

            private static final class WriteAnnotationVisitorImpl extends AnnotationVisitor {
                private final FieldAccess fieldAccess;

                public WriteAnnotationVisitorImpl(AnnotationVisitor visitor, FieldAccess fieldAccess) {
                    super(ASM5, visitor);
                    this.fieldAccess = fieldAccess;
                }

                @Override
                public void visit(String name, Object value) {
                    if("value".equals(name))
                        fieldAccess.setWrite(value.toString());
                    else if("owner".equals(name))
                        fieldAccess.setWriteOwner(value.toString());
                    else if("descriptor".equals(name))
                        fieldAccess.setWriteDescriptor(value.toString());
                    else if("isStatic".equals(name))
                        fieldAccess.setWriteStatic((Boolean) value);
                    super.visit(name, value);
                }
            }
        }
    }
}

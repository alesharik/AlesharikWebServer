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

package com.alesharik.webserver.api.serial;

import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.api.utils.classloader.GeneratedClassLoader;
import com.alesharik.webserver.internals.UnsafeAccess;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.objectweb.asm.Opcodes.*;

@UtilityClass
class SerializerFactory {
    private static final String MAGIC_ACCESSOR_IMPL = "sun/reflect/MagicAccessorImpl";
    private static final GeneratedClassLoader DEFAULT_CLASS_LOADER = new GeneratedClassLoader();
    private static final Map<ClassLoader, GeneratedClassLoader> classLoaders = new ConcurrentHashMap<>();
    private static final AtomicLong id = new AtomicLong(0);

    public static Serializer create(Class<?> clazz, AnnotationAdapter.Adapter adapter, double version) {
        List<Field> fields = getFields(clazz, adapter, version);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String name = "com/alesharik/webserver/api/serial/Serializer" + id.incrementAndGet();
        classWriter.visit(V1_8,
                ACC_SYNTHETIC | ACC_FINAL | ACC_PRIVATE,
                name,
                null,
                MAGIC_ACCESSOR_IMPL,
                new String[]{Type.getInternalName(Serializer.class)});

        visitGetNameMethod(classWriter, name);
        visitGetVersionMethod(classWriter, version);
        visitInit(classWriter);
        visitSerializeDefault(classWriter, fields);
        visitDeserializeDefaultIntoObject(classWriter, fields);
        visitDeserializeDefault(classWriter, clazz);
        visitSerialize(classWriter, clazz);
        visitDeserialize(classWriter, clazz);

        classWriter.visitEnd();
        String canonicalName = name.replace("/", ".");
        byte[] data = classWriter.toByteArray();

        GeneratedClassLoader loader = clazz.getClassLoader() == null
                ? DEFAULT_CLASS_LOADER
                : classLoaders.computeIfAbsent(clazz.getClassLoader(), GeneratedClassLoader::new);
        loader.addGeneratedClass(canonicalName, data);

        try {
            Constructor<?> ctor = loader.loadClass(canonicalName).getConstructor();
            ctor.setAccessible(true);
            return (Serializer) ctor.newInstance();
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    @NotNull
    private static List<Field> getFields(Class<?> clazz, AnnotationAdapter.Adapter adapter, double version) {
        List<Field> fields = new ArrayList<>();
        for(Field field : ReflectUtils.getAllDeclaredFields(clazz)) {
            if(Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            if(adapter.hasUntilAnnotation(field))
                if(version > adapter.getUntil(field))
                    continue;
            if(adapter.hasSinceAnnotation(field))
                if(version < adapter.getSince(field))
                    continue;
            fields.add(field);
        }
        return fields;
    }

    private static void visitGetVersionMethod(ClassWriter writer, double version) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "getVersion", "()D", null, null);
        m.visitCode();
        m.visitLdcInsn(version);
        m.visitInsn(DRETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static void visitGetNameMethod(ClassWriter writer, String name) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
        m.visitCode();
        m.visitLdcInsn(name);
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static void visitInit(ClassWriter writer) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        m.visitCode();
        m.visitVarInsn(ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        m.visitInsn(RETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static void visitSerializeDefault(ClassWriter writer, List<Field> fields) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "serializeDefault", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), null, null);
        m.visitCode();
        m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "create", Type.getMethodDescriptor(Type.getType(IOStream.class)), false);
        m.visitInsn(DUP);
        m.visitVarInsn(ASTORE, 2);
        //stream

        for(Field field : fields) {
            Class<?> type = field.getType();
            if(ClassUtils.isPrimitiveOrWrapper(type)) {
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 1);
                m.visitFieldInsn(GETFIELD, Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(type));
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(type)), true);
            } else if(type.isArray()) {
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 1);
                m.visitFieldInsn(GETFIELD, Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(type));
                writeArrayIntoStream(m, type);
            } else { //Object
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 1);
                m.visitTypeInsn(CHECKCAST, Type.getInternalName(field.getDeclaringClass()));
                m.visitFieldInsn(GETFIELD, Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(type));
                //stream, obj
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Serial.class), "serialize", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), false);
                m.visitInsn(DUP2);
                m.visitInsn(ARRAYLENGTH);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int.class)), true);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(byte[].class)), true);
            }
        }

        m.visitInsn(DUP);
        m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "toByteArray", Type.getMethodDescriptor(Type.getType(byte[].class)), true);
        m.visitInsn(SWAP);
        m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(IOStream.class)), false);
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    /**
     * Locals: 2 - IOStream
     * Stack: IOStream, array
     */
    private static void writeArrayIntoStream(MethodVisitor m, Class<?> clazz) {
        Class<?> component = clazz.getComponentType();

        Label end = new Label();
        Label notNull = new Label();
        m.visitInsn(DUP);
        m.visitJumpInsn(IFNONNULL, notNull);

        m.visitInsn(POP);
        m.visitLdcInsn(-1);
        m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int.class)), true);
        m.visitJumpInsn(GOTO, end);

        m.visitLabel(notNull);
        m.visitInsn(DUP);
        //stream, array, array
        m.visitInsn(ARRAYLENGTH);
        //stream, array, len
        m.visitVarInsn(ALOAD, 2);
        //stream, array, len, stream
        m.visitInsn(SWAP);
        //stream, array, stream, len
        m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int.class)), true);
        //stream, array

        Label forStatement = new Label();
        Label forStatementEnd = new Label();
        m.visitLdcInsn(0);
        m.visitLabel(forStatement);
        //stream, array, index
        m.visitInsn(SWAP);
        m.visitInsn(DUP);
        m.visitInsn(ARRAYLENGTH);
        //stream, index, array, len
        m.visitInsn(SWAP);
        //stream, index, len, array
        m.visitInsn(DUP_X2);
        m.visitInsn(POP);
        //stream, array, index, len
        m.visitInsn(SWAP);
        //stream, array, len, index
        m.visitInsn(DUP_X1);
        //stream, array, index, len, index
        m.visitInsn(SWAP);
        //stream, array, index, index, len
        m.visitJumpInsn(IF_ICMPGE, forStatementEnd);
        //stream, array, index
        m.visitInsn(SWAP);
        //stream, index, array
        m.visitInsn(DUP_X1);
        //stream, array, index, array
        m.visitInsn(SWAP);
        m.visitInsn(DUP_X1);
        //stream, array, index, array, index
        m.visitInsn(SWAP);
        //stream, array, index, index, array
        m.visitInsn(SWAP);
        m.visitInsn(getArrayLoadInstruction(component));
        //stream, array, index, object
        m.visitVarInsn(ALOAD, 2);
        //stream, array, index, object, stream
        m.visitInsn(SWAP);
        if(ClassUtils.isPrimitiveOrWrapper(component)) {
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(component)), true);
        } else if(component.isArray()) {
            writeArrayIntoStream(m, component);
        } else {//Object
            //stream, array, index, stream, object
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Serial.class), "serialize", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), false);
            m.visitInsn(DUP2);
            m.visitInsn(ARRAYLENGTH);
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(int.class)), true);
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "write", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(byte[].class)), true);
            //stream, array, index
        }

        m.visitLdcInsn(1);
        m.visitInsn(IADD);
        m.visitJumpInsn(GOTO, forStatement);

        m.visitLabel(forStatementEnd);
        m.visitInsn(POP2);
        m.visitInsn(POP);
        m.visitLabel(end);
    }

    private static void visitDeserializeDefault(ClassWriter writer, Class<?> clazz) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "deserializeDefault", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(byte[].class)), null, null);
        m.visitCode();
        m.visitVarInsn(ALOAD, 0);
        m.visitVarInsn(ALOAD, 1);
        m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "create", Type.getMethodDescriptor(Type.getType(IOStream.class), Type.getType(byte[].class)), false);
        m.visitInsn(DUP);
        m.visitVarInsn(ASTORE, 3);
        m.visitTypeInsn(NEW, Type.getInternalName(clazz));
        try {
            clazz.getConstructor();
            m.visitInsn(DUP);
            m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(clazz), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        } catch (NoSuchMethodException ignored) {
        }
        m.visitInsn(DUP);
        m.visitVarInsn(ASTORE, 4);
        m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Serializer.class), "deserializeDefaultIntoObject", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(IOStream.class), Type.getType(Object.class)), true);
        m.visitVarInsn(ALOAD, 3);
        m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "recycle", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(IOStream.class)), false);
        m.visitVarInsn(ALOAD, 4);
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static void visitDeserializeDefaultIntoObject(ClassWriter writer, List<Field> fields) {
        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "deserializeDefaultIntoObject", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(IOStream.class), Type.getType(Object.class)), null, null);
        m.visitCode();
        m.visitVarInsn(ALOAD, 1);
        m.visitVarInsn(ALOAD, 2);
        m.visitInsn(SWAP);
        //Stack: Instance, IOStream
        for(Field field : fields) {
            Class<?> type = field.getType();
            if(ClassUtils.isPrimitiveOrWrapper(type)) {
                m.visitInsn(DUP2);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), getMethodNameFromPrimitiveType(type), Type.getMethodDescriptor(Type.getType(type)), true);
                m.visitLdcInsn(UnsafeAccess.INSTANCE.objectFieldOffset(field));
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(SerializerFactory.class), getMethodNameForUnsafeStore(type), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(type), Type.getType(long.class)), false);
            } else if(type.isArray()) {
                m.visitInsn(DUP2);
                readArrayFromStream(m, field.getType());
                m.visitLdcInsn(UnsafeAccess.INSTANCE.objectFieldOffset(field));
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(SerializerFactory.class), getMethodNameForUnsafeStore(type), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(Object.class), Type.getType(long.class)), false);
            } else {
                m.visitInsn(DUP2);
                //Instance, stream, Instance, stream
                m.visitInsn(DUP);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "readint", Type.getMethodDescriptor(Type.getType(int.class)), true);
                //instance, stream, Instance, stream, size

                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "read", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(int.class)), true);
                //instance, stream, Instance, b]
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Serial.class), "deserialize", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(byte[].class)), false);
                m.visitTypeInsn(CHECKCAST, Type.getInternalName(type));

                //instance, stream, instance, obj
                m.visitLdcInsn(UnsafeAccess.INSTANCE.objectFieldOffset(field));
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(SerializerFactory.class), getMethodNameForUnsafeStore(type), Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(Object.class), Type.getType(long.class)), false);
            }
        }
        m.visitInsn(POP);
        m.visitInsn(DUP);
        m.visitInsn(RETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    /**
     * Stack: Instance, IOStream
     * Awaiting: Instance, array
     * Locals: 1 - IOStream, 2 - Instance
     */
    private static void readArrayFromStream(MethodVisitor m, Class<?> clazz) {
        m.visitInsn(DUP);
        m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "readint", Type.getMethodDescriptor(Type.getType(int.class)), true);

        Label end = new Label();
        Label notNull = new Label();

        m.visitInsn(DUP);
        m.visitLdcInsn(-1);
        m.visitJumpInsn(IF_ICMPGT, notNull);
        //Instance, stream, int
        m.visitInsn(POP2);
        m.visitInsn(ACONST_NULL);
        m.visitJumpInsn(GOTO, end);

        m.visitLabel(notNull);
        if(clazz.getComponentType().isPrimitive())
            m.visitIntInsn(NEWARRAY, getPrimitiveType(clazz.getComponentType()));
        else
            m.visitTypeInsn(ANEWARRAY, Type.getInternalName(clazz.getComponentType()));
        //Instance, IOStream, arr

        Label forStatementEnd = new Label();
        Label forStatement = new Label();
        m.visitLdcInsn(0);

        //Instance, IOStream, arr, idx
        m.visitLabel(forStatement);
        m.visitInsn(DUP);
        //Ins, Str, arr, idx, idx
        m.visitInsn(DUP_X2);
        //Ins, str, idx, arr, idx, idx
        m.visitInsn(POP2);
        m.visitInsn(DUP);
        m.visitInsn(ARRAYLENGTH);
        //Ins, str, idx, arr, len
        m.visitInsn(SWAP);
        m.visitInsn(DUP_X2);
        m.visitInsn(POP);
        //Ins, str, arr, idx, len
        m.visitInsn(SWAP);
        m.visitInsn(DUP_X1);
        m.visitInsn(SWAP);
        //Ins, Str, arr, idx, idx, len
        m.visitJumpInsn(IF_ICMPGE, forStatementEnd);
        //Ins, Str, arr, idx
        m.visitInsn(DUP2);
        m.visitVarInsn(ALOAD, 1);
        //Ins, Str, arr, idx, arr, idx, stream
        if(ClassUtils.isPrimitiveOrWrapper(clazz.getComponentType())) {
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), getMethodNameFromPrimitiveType(clazz.getComponentType()), Type.getMethodDescriptor(Type.getType(clazz.getComponentType())), true);
        } else if(clazz.getComponentType().isArray()) {
            m.visitVarInsn(ALOAD, 2);
            m.visitInsn(SWAP);
            readArrayFromStream(m, clazz.getComponentType());
            m.visitInsn(SWAP);
            m.visitInsn(POP);
        } else {
            m.visitInsn(DUP);
            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "readint", Type.getMethodDescriptor(Type.getType(int.class)), true);

            m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "read", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(int.class)), true);
            //instance, stream, Instance, b]
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Serial.class), "deserialize", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(byte[].class)), false);
            m.visitTypeInsn(CHECKCAST, Type.getInternalName(clazz.getComponentType()));
        }
        //Ins, Str, arr, idx, arr, idx, obj
        m.visitInsn(getArrayStoreInstruction(clazz.getComponentType()));
        m.visitLdcInsn(1);
        m.visitInsn(IADD);
        m.visitJumpInsn(GOTO, forStatement);

        m.visitLabel(forStatementEnd);
        m.visitInsn(POP);
        m.visitInsn(SWAP);
        m.visitInsn(POP);
        m.visitLabel(end);
    }

    private static void visitSerialize(ClassWriter writer, Class<?> clazz) {
        boolean hasWriteReplace = false;
        boolean hasWriteObject = false;
        for(Method method : ReflectUtils.getAllDeclaredMethods(clazz)) {
            if("writeReplace".equals(method.getName()) && method.getReturnType().equals(Object.class) && method.getParameterCount() == 0)
                hasWriteReplace = true;
            else if("writeObject".equals(method.getName()) && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(ObjectOutputStream.class) && method.getReturnType().equals(void.class))
                hasWriteObject = true;
        }

        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "serialize", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), null, null);
        m.visitCode();
        if(hasWriteReplace) {
            m.visitVarInsn(ALOAD, 1);
            m.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(clazz), "writeReplace", Type.getMethodDescriptor(Type.getType(Object.class)), false);
            m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Serial.class), "serialize", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), false);
        } else {
            if(Externalizable.class.isAssignableFrom(clazz)) {
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "create", Type.getMethodDescriptor(Type.getType(IOStream.class)), false);
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 0);
                m.visitVarInsn(ALOAD, 1);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "objectOutput", Type.getMethodDescriptor(Type.getType(IOStream.ObjectOutputImpl.class), Type.getType(Serializer.class), Type.getType(Object.class)), true);
                m.visitVarInsn(ALOAD, 1);
                m.visitTypeInsn(CHECKCAST, Type.getInternalName(Externalizable.class));
                m.visitInsn(SWAP);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Externalizable.class), "writeExternal", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectOutput.class)), true);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "toByteArray", Type.getMethodDescriptor(Type.getType(byte[].class)), true);
            } else if(hasWriteObject) {//Serializable
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "create", Type.getMethodDescriptor(Type.getType(IOStream.class)), false);
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 0);
                m.visitVarInsn(ALOAD, 1);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "objectOutput", Type.getMethodDescriptor(Type.getType(IOStream.ObjectOutputImpl.class), Type.getType(Serializer.class), Type.getType(Object.class)), true);
                m.visitVarInsn(ALOAD, 1);
                m.visitInsn(SWAP);
                m.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(clazz), "writeObject", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectOutputStream.class)), false);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "toByteArray", Type.getMethodDescriptor(Type.getType(byte[].class)), true);
            } else {
                m.visitVarInsn(ALOAD, 1);
                m.visitVarInsn(ALOAD, 0);
                m.visitInsn(SWAP);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Serializer.class), "serializeDefault", Type.getMethodDescriptor(Type.getType(byte[].class), Type.getType(Object.class)), true);
            }
        }
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static void visitDeserialize(ClassWriter writer, Class<?> clazz) {
        boolean hasReadResolve = false;
        boolean hasReadObject = false;
        boolean hasReadObjectNoData = false;
        for(Method method : ReflectUtils.getAllDeclaredMethods(clazz)) {
            if("readResolve".equals(method.getName()) && method.getReturnType().equals(Object.class) && method.getParameterCount() == 0)
                hasReadResolve = true;
            else if("readObject".equals(method.getName()) && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(ObjectInputStream.class) && Modifier.isPrivate(method.getModifiers()) && method.getReturnType().equals(void.class))
                hasReadObject = true;
            else if("readObjectNoData".equals(method.getName()) && method.getParameterCount() == 0 && Modifier.isPrivate(method.getModifiers()) && method.getReturnType().equals(void.class))
                hasReadObjectNoData = true;
        }

        MethodVisitor m = writer.visitMethod(ACC_PUBLIC, "deserialize", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(byte[].class)), null, null);
        m.visitCode();
        if(hasReadResolve) {
            m.visitTypeInsn(NEW, Type.getInternalName(clazz));
            try {
                clazz.getConstructor();
                m.visitInsn(DUP);
                m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(clazz), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            } catch (NoSuchMethodException ignored) {
            }
            m.visitInsn(DUP);
            m.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(clazz), "readResolve", Type.getMethodDescriptor(Type.getType(Object.class)), false);
        } else {
            Label end = new Label();

            if(hasReadObjectNoData) {
                Label readObjectNoDataEnd = new Label();
                m.visitVarInsn(ALOAD, 1);
                m.visitInsn(ARRAYLENGTH);
                m.visitLdcInsn(0);
                m.visitJumpInsn(IF_ICMPNE, readObjectNoDataEnd);
                m.visitTypeInsn(NEW, Type.getInternalName(clazz));
                try {
                    clazz.getConstructor();
                    m.visitInsn(DUP);
                    m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(clazz), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
                } catch (NoSuchMethodException ignored) {
                }
                m.visitInsn(DUP);
                m.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(clazz), "readObjectNoData", Type.getMethodDescriptor(Type.getType(void.class)), false);
                m.visitJumpInsn(GOTO, end);
                m.visitLabel(readObjectNoDataEnd);
            }

            if(hasReadObject) {
                m.visitTypeInsn(NEW, Type.getInternalName(clazz));
                try {
                    clazz.getConstructor();
                    m.visitInsn(DUP);
                    m.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(clazz), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
                } catch (NoSuchMethodException ignored) {
                }
                m.visitInsn(DUP);
                m.visitVarInsn(ASTORE, 2);
                m.visitInsn(DUP);
                m.visitVarInsn(ALOAD, 1);
                m.visitMethodInsn(INVOKESTATIC, Type.getInternalName(IOStream.Factory.class), "create", Type.getMethodDescriptor(Type.getType(IOStream.class), Type.getType(byte[].class)), false);
                m.visitVarInsn(ALOAD, 0);
                m.visitVarInsn(ALOAD, 2);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(IOStream.class), "objectInput", Type.getMethodDescriptor(Type.getType(ObjectInputStream.class), Type.getType(Serializer.class), Type.getType(Object.class)), true);
                m.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(clazz), "readObject", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ObjectInputStream.class)), false);
            } else {
                m.visitVarInsn(ALOAD, 0);
                m.visitVarInsn(ALOAD, 1);
                m.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Serializer.class), "deserializeDefault", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(byte[].class)), true);
            }

            m.visitLabel(end);
        }
        m.visitInsn(ARETURN);
        m.visitMaxs(-1, -1);
        m.visitEnd();
    }

    private static int getPrimitiveType(Class<?> clazz) {
        if(clazz == int.class)
            return T_INT;
        else if(clazz == long.class)
            return T_LONG;
        else if(clazz == short.class)
            return T_SHORT;
        else if(clazz == byte.class)
            return T_BYTE;
        else if(clazz == boolean.class)
            return T_BOOLEAN;
        else if(clazz == char.class)
            return T_CHAR;
        else if(clazz == float.class)
            return T_FLOAT;
        else if(clazz == double.class)
            return T_DOUBLE;
        else
            throw new IllegalArgumentException(clazz + " is not a primitive!");
    }

    private static int getArrayStoreInstruction(Class<?> clazz) {
        if(clazz == int.class)
            return IASTORE;
        else if(clazz == long.class)
            return LASTORE;
        else if(clazz == short.class)
            return SASTORE;
        else if(clazz == byte.class)
            return BASTORE;
        else if(clazz == boolean.class)
            return BASTORE;
        else if(clazz == char.class)
            return CASTORE;
        else if(clazz == float.class)
            return FASTORE;
        else if(clazz == double.class)
            return DASTORE;
        else
            return AASTORE;
    }

    private static int getArrayLoadInstruction(Class<?> clazz) {
        if(clazz == byte.class)
            return BALOAD;
        else if(clazz == char.class)
            return CALOAD;
        else if(clazz == double.class)
            return DALOAD;
        else if(clazz == float.class)
            return FALOAD;
        else if(clazz == long.class)
            return LALOAD;
        else if(clazz.isPrimitive())
            return IALOAD;
        else
            return AALOAD;
    }

    private static String getMethodNameFromPrimitiveType(Class<?> type) {
        if(type == int.class)
            return "readint";
        else if(type == Integer.class)
            return "readInteger";
        else if(type == long.class)
            return "readlong";
        else if(type == Long.class)
            return "readLong";
        else if(type == short.class)
            return "readshort";
        else if(type == Short.class)
            return "readShort";
        else if(type == byte.class)
            return "readbyte";
        else if(type == Byte.class)
            return "readByte";
        else if(type == float.class)
            return "readfloat";
        else if(type == Float.class)
            return "readFloat";
        else if(type == double.class)
            return "readdouble";
        else if(type == Double.class)
            return "readDouble";
        else if(type == char.class)
            return "readchar";
        else if(type == Character.class)
            return "readCharacter";
        else if(type == boolean.class)
            return "readboolean";
        else if(type == Boolean.class)
            return "readBoolean";
        return null;
    }

    private static String getMethodNameForUnsafeStore(Class<?> type) {
        if(type == int.class)
            return "putInt";
        else if(type == long.class)
            return "putLong";
        else if(type == short.class)
            return "putShort";
        else if(type == byte.class)
            return "putByte";
        else if(type == float.class)
            return "putFloat";
        else if(type == double.class)
            return "putDouble";
        else if(type == char.class)
            return "putChar";
        else if(type == boolean.class)
            return "putBoolean";
        else
            return "putObject";
    }

    @SuppressWarnings("unused")
    private static void putLong(Object o, long l, long offset) {
        UnsafeAccess.INSTANCE.putLong(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putShort(Object o, short l, long offset) {
        UnsafeAccess.INSTANCE.putShort(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putInt(Object o, int l, long offset) {
        UnsafeAccess.INSTANCE.putInt(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putByte(Object o, byte l, long offset) {
        UnsafeAccess.INSTANCE.putByte(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putBoolean(Object o, boolean l, long offset) {
        UnsafeAccess.INSTANCE.putBoolean(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putChar(Object o, char l, long offset) {
        UnsafeAccess.INSTANCE.putChar(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putFloat(Object o, float l, long offset) {
        UnsafeAccess.INSTANCE.putFloat(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putDouble(Object o, double l, long offset) {
        UnsafeAccess.INSTANCE.putDouble(o, offset, l);
    }

    @SuppressWarnings("unused")
    private static void putObject(Object o, Object l, long offset) {
        UnsafeAccess.INSTANCE.putObject(o, offset, l);
    }

    public static void unloadClassLoader(ClassLoader classLoader) {
        classLoaders.remove(classLoader);
    }
}

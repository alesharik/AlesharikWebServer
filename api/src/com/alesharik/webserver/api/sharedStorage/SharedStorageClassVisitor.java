package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.ConcurrentCompletableFuture;
import lombok.SneakyThrows;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.objectweb.asm.Opcodes.*;

final class SharedStorageClassVisitor extends ClassAdapter {
    private boolean transform = false;
    private ConcurrentCompletableFuture<String> storageName;
    private String id = "";

    public SharedStorageClassVisitor(ClassVisitor cv) {
        super(cv);
    }

    private void registerId(String name) {
        String id = UUID.randomUUID().toString();
        GetterSetterManager.register(id, name);
        this.id = id;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if(desc.equals("Lcom/alesharik/webserver/api/sharedStorage/annotations/UseSharedStorage;")) { //Use my annotation
            ConcurrentCompletableFuture<String> future = new ConcurrentCompletableFuture<>();
            transform = true;
            storageName = future;
            return new AnnotationValueExtractor(future);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    @SneakyThrows
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(transform) {
            if(id.isEmpty()) {
                registerId(storageName.get());
            }
            return new MethodReplacer(super.visitMethod(access, name, desc, signature, exceptions), id, desc, desc.substring(desc.indexOf('(') + 1, desc.indexOf(')')).split(";").length, desc);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    /**
     * Use for extract annotation value
     */
    private static final class AnnotationValueExtractor implements AnnotationVisitor {
        private ConcurrentCompletableFuture<String> ret;

        public AnnotationValueExtractor(ConcurrentCompletableFuture<String> string) {
            ret = string;
        }

        @Override
        public void visit(String name, Object value) {
            if(name.equals("value")) {
                ret.set((String) value);
            }
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return null;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return null;
        }

        @Override
        public void visitEnd() {
        }
    }

    private static final class MethodReplacer extends MethodAdapter {
        private String ret;
        private String id;
        private ConcurrentCompletableFuture<String> result;
        private int type = -1; //0 - setter, 1 - getter, -1 - ops
        private int argCount;
        private Type[] args;

        public MethodReplacer(MethodVisitor mv, String id, String ret, int agrCount, String desc) {
            super(mv);
            this.id = id;
            this.result = new ConcurrentCompletableFuture<>();
            this.ret = ret;
            this.argCount = agrCount;
            this.args = Type.getArgumentTypes(desc);
        }

        public MethodReplacer(MethodVisitor mv) {
            super(mv);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            switch (desc) {
                case "Lcom/alesharik/webserver/api/sharedStorage/annotations/SharedValueGetter;": //Getter
                    type = 1;
                    return new AnnotationValueExtractor(result);
                case "Lcom/alesharik/webserver/api/sharedStorage/annotations/SharedValueSetter;": //Setter
                    type = 0;
                    return new AnnotationValueExtractor(result);
                default:
                    return super.visitAnnotation(desc, visible);
            }
        }

        @Override
        public void visitCode() {
            try {
                if(type > -1) {
                    super.visitCode();
                    if(type == 1) {// Is a get method //TODO add support to primitives
                        mv.visitCode();
                        mv.visitLdcInsn(id); // First parameter - id
                        mv.visitLdcInsn(result.get()); // Second parameter - field name
                        mv.visitMethodInsn(INVOKESTATIC, "com/alesharik/webserver/api/sharedStorage/GetterSetterManager", "get", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"); // Invoke method

                        if(ret.contains(";")) {
                            Type returnType = Type.getReturnType(ret);
                            mv.visitTypeInsn(CHECKCAST, getBoxedType(returnType).getInternalName()); // check cast
                            unBoxForSignature(getTypeSignature(returnType.getSort()));
                        } else {
                            mv.visitTypeInsn(CHECKCAST, ret.substring(ret.indexOf("()") + 3, ret.lastIndexOf(";"))); //check cast
                        }
                        mv.visitInsn(ARETURN); //Return
                        mv.visitEnd();
                    } else if(type == 0) { // Is a set method
                        if(argCount <= 0) {
                            System.out.println("Can't transform method! Setter must have args > 0!");
                            mv.visitEnd();
                            return;
                        }
                        Type arg = args[0];
                        Type boxedArg = getBoxedType(arg);
                        mv.visitCode();
                        mv.visitLdcInsn(id); // First parameter - id
                        mv.visitLdcInsn(result.get()); // Second parameter - field name
                        mv.visitVarInsn(getLoadOpcodeForType(arg), 1); // load var
                        String typeSignature = String.valueOf(getTypeSignature(arg.getSort()));
                        if(!typeSignature.equals("L")) {
                            mv.visitMethodInsn(INVOKESTATIC, boxedArg.getInternalName(), "valueOf", "(" + typeSignature + ")L" + boxedArg.getInternalName() + ";");
                        }
                        mv.visitMethodInsn(INVOKESTATIC, "com/alesharik/webserver/api/sharedStorage/GetterSetterManager", "set", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"); // Set method
                        mv.visitInsn(RETURN); // Return
                        mv.visitEnd();
                    }
                } else {
                    super.visitCode();
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e);
            }
        }

        private static int getLoadOpcodeForType(Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    return ILOAD;
                case Type.LONG:
                    return LLOAD;
                case Type.FLOAT:
                    return FLOAD;
                case Type.DOUBLE:
                    return DLOAD;
                default:
                    return ALOAD;
            }
        }

        private static Type getBoxedType(final Type type) {
            switch (type.getSort()) {
                case Type.BYTE:
                    return Type.getType(Byte.class);
                case Type.BOOLEAN:
                    return Type.getType(Boolean.class);
                case Type.SHORT:
                    return Type.getType(Short.class);
                case Type.CHAR:
                    return Type.getType(Character.class);
                case Type.INT:
                    return Type.getType(Integer.class);
                case Type.LONG:
                    return Type.getType(Long.class);
                case Type.FLOAT:
                    return Type.getType(Float.class);
                case Type.DOUBLE:
                    return Type.getType(Double.class);
                default:
                    return type;
            }
        }

//        private static Type getUnboxedType(Type type) {
//            switch (type) {
//                case Type.BYTE_TYPE:
//                    return Type.BYTE;
//                case Type.BOOLEAN_TYPE:
//                    return Type.BOOLEAN_TYPE;
//                case Type.SHORT_TYPE:
//                    return Type.SHORT_TYPE;
//                case Type.CHAR_TYPE:
//                    return Type.CHAR_TYPE;
//                case Type.INT_TYPE:
//                    return Type.INT_TYPE;
//                case Type.LONG_TYPE:
//                    return Type.LONG_TYPE;
//                case Type.FLOAT_TYPE:
//                    return Type.FLOAT_TYPE;
//                case Type.DOUBLE_TYPE:
//                    return Type.DOUBLE_TYPE;
//                default:
//                    return type;
//            }
//        }

        private char getTypeSignature(int type) {
            switch (type) {
                case Type.BYTE:
                    return 'B';
                case Type.BOOLEAN:
                    return 'Z';
                case Type.CHAR:
                    return 'C';
                case Type.SHORT:
                    return 'S';
                case Type.INT:
                    return 'I';
                case Type.LONG:
                    return 'J';
                case Type.FLOAT:
                    return 'F';
                case Type.DOUBLE:
                    return 'D';
                default:
                    return 'L';
            }
        }

        private void unBoxForSignature(char signature) {
            switch (signature) {
                case 'B':

            }
        }
    }
}

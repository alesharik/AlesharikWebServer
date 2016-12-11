package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.ConcurrentCompletableFuture;
import lombok.SneakyThrows;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

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
            return new MethodReplacer(super.visitMethod(access, name, desc, signature, exceptions), id, desc, desc.substring(desc.indexOf('(') + 1, desc.indexOf(')')).split(";").length);
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

        public MethodReplacer(MethodVisitor mv, String id, String ret, int agrCount) {
            super(mv);
            this.id = id;
            this.result = new ConcurrentCompletableFuture<>();
            this.ret = ret;
            this.argCount = agrCount;
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
                    if(type == 1) {// Is a getIpForMicroservice method
                        mv.visitCode();
                        mv.visitLdcInsn(id); // First parameter - id
                        mv.visitLdcInsn(result.get()); // Second parameter - field name
                        mv.visitMethodInsn(INVOKESTATIC, "com/alesharik/webserver/api/sharedStorage/GetterSetterManager", "getIpForMicroservice", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"); // Invoke method
                        mv.visitTypeInsn(CHECKCAST, ret.substring(ret.indexOf("()") + 3, ret.lastIndexOf(";"))); // Cast to return var
                        mv.visitInsn(ARETURN); //Return
                        mv.visitMaxs(2, 1);
                        mv.visitEnd();
                    } else if(type == 0) { // Is a set method
                        if(argCount <= 0) {
                            System.out.println("Can't transform method! Setter must have args > 0!");
                            mv.visitEnd();
                            return;
                        }

                        mv.visitCode();
                        mv.visitLdcInsn(id); // First parameter - id
                        mv.visitLdcInsn(result.get()); // Second parameter - field name
                        mv.visitVarInsn(ALOAD, 1); // load var
                        mv.visitMethodInsn(INVOKESTATIC, "com/alesharik/webserver/api/sharedStorage/GetterSetterManager", "set", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"); // Set method
                        mv.visitInsn(RETURN); // Return
                        mv.visitMaxs(3, 2);
                        mv.visitEnd();
                    }
                } else {
                    super.visitCode();
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e);
            }
        }

    }
}

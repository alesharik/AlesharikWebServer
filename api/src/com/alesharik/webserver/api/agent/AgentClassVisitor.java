package com.alesharik.webserver.api.agent;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import java.util.concurrent.atomic.AtomicBoolean;

class AgentClassVisitor extends ClassAdapter {
    private static final String CLASS_LOADER_ANNOTATION_DESCRIPTION = "Lcom/alesharik/webserver/api/agent/ClassTransformer;";
    private static final String TRANSFORM_ANNOTATION_DESCRIPTION = "Lcom/alesharik/webserver/api/agent/Transform;";
    private static final String TRANSFORM_ALL_ANNOTATION_DESCRIPTION = "Lcom/alesharik/webserver/api/agent/TransformAll;";

    private final AtomicBoolean isOk;

    private boolean isAnnotationPresent = false;
    private AtomicBoolean isAnyMethodPresent = new AtomicBoolean(false);

    public AgentClassVisitor(ClassVisitor classVisitor, AtomicBoolean isOk) {
        super(classVisitor);
        this.isOk = isOk;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean isVisible) {
        if(desc.equals(CLASS_LOADER_ANNOTATION_DESCRIPTION)) {
            isAnnotationPresent = true;
        }
        return super.visitAnnotation(desc, isVisible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if(isAnnotationPresent && !isAnyMethodPresent.get()) {
            return new MethodChecker(methodVisitor, isAnyMethodPresent);
        }
        return methodVisitor;
    }

    @Override
    public void visitEnd() {
        isOk.set(isAnnotationPresent && isAnyMethodPresent.get());
        super.visitEnd();
    }

    private static final class MethodChecker extends MethodAdapter {
        private final AtomicBoolean is;

        public MethodChecker(MethodVisitor methodVisitor, AtomicBoolean is) {
            super(methodVisitor);
            this.is = is;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean isVisible) {
            if(desc.equals(AgentClassVisitor.TRANSFORM_ANNOTATION_DESCRIPTION) || desc.equals(AgentClassVisitor.TRANSFORM_ALL_ANNOTATION_DESCRIPTION)) {
                is.set(true);
            }
            return super.visitAnnotation(desc, isVisible);
        }
    }
}

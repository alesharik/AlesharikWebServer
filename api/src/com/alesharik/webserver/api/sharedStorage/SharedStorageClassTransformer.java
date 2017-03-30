package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@ClassTransformer
public final class SharedStorageClassTransformer {

    @TransformAll
    public static byte[] transform(@Param(Param.Type.CLASS_LOADER) ClassLoader loader, @Param(Param.Type.CLASS_NAME) String className, @Param(Param.Type.CLASS_BEING_REDEFINED) Class<?> classBeingRedefined, @Param(Param.Type.PROTECTION_DOMAIN) ProtectionDomain protectionDomain, @Param(Param.Type.CLASSFILE_BUFFER) byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classfileBuffer);
            classReader.accept(classNode, 0);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            classNode.accept(new SharedStorageClassVisitor(classWriter));
            return classWriter.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(className);
            System.exit(0);
        }
        return classfileBuffer;
    }
}

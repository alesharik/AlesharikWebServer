package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.agent.ClassTransformer;
import com.alesharik.webserver.api.agent.Param;
import com.alesharik.webserver.api.agent.TransformAll;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

@ClassTransformer
public final class SharedStorageClassTransformer {

    @TransformAll
    public static byte[] transform(@Param(Param.Type.CLASS_LOADER) ClassLoader loader, @Param(Param.Type.CLASS_NAME) String className, @Param(Param.Type.CLASS_BEING_REDEFINED) Class<?> classBeingRedefined, @Param(Param.Type.PROTECTION_DOMAIN) ProtectionDomain protectionDomain, @Param(Param.Type.CLASSFILE_BUFFER) byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            classReader.accept(new SharedStorageClassVisitor(classWriter), 0);
            return classWriter.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }

}

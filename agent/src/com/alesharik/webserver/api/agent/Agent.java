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

package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.logger.Prefixes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Set;

/**
 * Java Agent of AlesharikWebServer
 *
 * @implNote If you create custom server, you MUST use this agent
 */
@Prefixes("[Agent]")
@ExecutionStage.AuthorizedImpl
public final class Agent {
    private static Instrumentation instrumentation;

    /**
     * DO NOT CALL IT AFTER MAIN!
     * @implNote you need to call this in premain
     */
    public static void premain(@Nullable String agentArgs, @Nonnull Instrumentation inst) {
        if(instrumentation != null)
            throw new IllegalStateException("WTF ARE YOU DOING?");
        instrumentation = inst;

        ExecutionStage.setState(ExecutionStage.AGENT);

        inst.addTransformer(new ClassPathScannerTransformer(), false);
        inst.addTransformer(new AgentClassTransformer(), true);

        System.out.println("Agent successfully installed! Class redefinition supported: " + inst.isRedefineClassesSupported() + ", class retransformation supported: " + inst.isRetransformClassesSupported());
    }

    public static void agentmain(@Nullable String agentArgs, @Nonnull Instrumentation instrumentation) {
        premain(agentArgs, instrumentation);
    }

    /**
     * @see Instrumentation#retransformClasses(Class[])
     */
    public static void retransform(Class<?> clazz) throws UnmodifiableClassException {
        instrumentation.retransformClasses(clazz);
    }

    /**
     * @see Instrumentation#redefineClasses(ClassDefinition...)
     */
    public static void redefine(ClassDefinition clazz) throws UnmodifiableClassException, ClassNotFoundException {
        instrumentation.redefineClasses(clazz);
    }

    /**
     * @see Instrumentation#getInitiatedClasses(ClassLoader)
     */
    public static Class[] getAllInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }

    /**
     * Return all existing class loaders
     */
    public static Set<ClassLoader> getAllLoadedClassLoaders() {
        return ClassPathScannerTransformer.getClassLoaders();
    }

    /**
     * Scan custom classloader
     */
    public static void tryScanClassLoader(@Nonnull ClassLoader classLoader) {
        ClassPathScannerTransformer.tryScanClassLoader(classLoader);
    }

    public static void unloadClassLoader(@Nonnull ClassLoader classLoader) {
        AgentClassTransformer.removeClassLoader(classLoader);
        ClassPathScannerTransformer.removeClassLoader(classLoader);
    }

    /**
     * Return <code>true</code> if ClassPathScanner scanning some classloaders
     */
    public static boolean isScanning() {
        return !ClassPathScannerTransformer.isFree();
    }

    /**
     * Shutdown agent's threads
     */
    public static void shutdown() {
        ClassPathScannerTransformer.shutdown();
    }

    public static boolean isClassRedefinitionSupported() {
        return instrumentation.isRedefineClassesSupported();
    }

    public static boolean isClassRetransformationSupported() {
        return instrumentation.isRetransformClassesSupported();
    }

    /**
     * Check agent installation. If agent is not installed, it install in on the current VM
     */
    public static void checkInstall() {
        if(instrumentation == null)
            AgentInstaller.install();
    }
}

package com.alesharik.webserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public final class TestUtils {
    private TestUtils() {
    }

    /**
     * Check if class is utility class
     *
     * @param clazz the class
     */
    public static void assertUtilityClass(Class<?> clazz) {
        if(!Modifier.isFinal(clazz.getModifiers())) {
            throw new AssertionError("Utility class must be final!");
        }
        Constructor<?>[] constructors = clazz.getConstructors();
        for(Constructor<?> constructor : constructors) {
            if(!Modifier.isPrivate(constructor.getModifiers())) {
                throw new AssertionError("Utility class must have no public/protected constructors!");
            }
            if(constructor.getParameterCount() > 0) {
                throw new AssertionError("Utility class can't have constructor with arguments!");
            }
            constructor.setAccessible(true);
            try {
                constructor.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                if(!(e.getCause() instanceof UnsupportedOperationException)) {
                    throw new AssertionError("Utility class constructor must throw UnsupportedOperationException!");
                }
            }
        }
    }
}

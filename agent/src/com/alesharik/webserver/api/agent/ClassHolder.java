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

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Use this to correctly hold classes from ClassPathScanner
 */
@Deprecated
@UtilityClass
public class ClassHolder {
    private static final List<ClassHoldingContext> contexts = new ArrayList<>();
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Register new class holder context
     * @param classHoldingContext the class holder context
     * @throws IllegalArgumentException if the context has already registered
     */
    public static void register(@Nonnull ClassHoldingContext classHoldingContext) {
        lock.lock();
        try {
            if(contexts.contains(classHoldingContext))
                throw new IllegalArgumentException("Holder has already had this context!");
            contexts.add(classHoldingContext);
            classHoldingContext.create();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unregister new class holder context
     * @param classHoldingContext the class holder context
     */
    public static void unregister(@Nonnull ClassHoldingContext classHoldingContext) {
        lock.lock();
        try {
            if(contexts.remove(classHoldingContext))
                classHoldingContext.destroy();
        } finally {
            lock.unlock();
        }
    }

    static void pause() {
        for(ClassHoldingContext context : contexts)
            context.pause();
    }

    static void resume() {
        for(ClassHoldingContext context : contexts)
            context.resume();
    }

    static void rescan(Class<?> clazz) {
        for(ClassHoldingContext context : contexts)
            context.reload(clazz);
    }
}

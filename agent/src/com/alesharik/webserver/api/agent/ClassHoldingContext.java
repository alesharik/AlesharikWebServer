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

import javax.annotation.Nonnull;

/**
 * This context must control all classes from ClassPathScanner
 */
public interface ClassHoldingContext {
    /**
     * Called when context is created
     */
    void create();

    /**
     * Called before ClassPathScanner method
     * @param clazz the class to reload
     */
    void reload(@Nonnull Class<?> clazz);

    /**
     * Called before class reloading
     */
    void pause();

    /**
     * Called after class reloading. MUST free all old classes and class loaders
     */
    void resume();

    /**
     * Called when context is destroyed
     */
    void destroy();
}

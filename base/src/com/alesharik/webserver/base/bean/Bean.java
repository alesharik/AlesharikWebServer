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

package com.alesharik.webserver.base.bean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated class will be processed as bean
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
    /**
     * Return <code>true</code> if this class is singleton, otherwise <code>false</code>
     */
    boolean singleton() default false;

    /**
     * If class is not a singleton, return value will be ignored.
     * Return <code>true</code> if class must be instantiated after registration. <code>false</code> means that class
     * will be instantiated after first request
     */
    @Deprecated
    boolean instantlyInstantiated() default true;

    /**
     * Return class factory
     *
     * @return class factory. {@link BeanFactory} means that class must be instantiated with default constructor. Default constructor is constructor with less argument count or {@link DefaultConstructor}
     */
    Class<? extends BeanFactory> factory() default BeanFactory.class;

    /**
     * Autowire enable automatic wiring for all fields. {@link Wire} can disable it for filed
     */
    boolean autowire() default true;

    /**
     * Return <code>true</code> if this class can be collected by GC. <code>false</code> will create storing reference.
     * WARNING! Can create memory leaks!
     */
    boolean canBeGarbage() default true;
}

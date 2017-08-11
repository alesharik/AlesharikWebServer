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

package com.alesharik.database.entity.asm;

import com.alesharik.database.entity.Entity;
import jdk.internal.org.objectweb.asm.Type;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class EntityClassManager {
    private static final Map<String, PreloadEntityDescription> preloadEntityDescriptions = new ConcurrentHashMap<>();
    private static final Map<Class<?>, EntityDescription> entityDescriptions = new ConcurrentHashMap<>();

    static void addPreloadEntityDescription(String internalName, PreloadEntityDescription description) {
        preloadEntityDescriptions.put(internalName, description);
    }

    public static EntityDescription getEntityDescription(Class<?> clazz) {
        if(!isEntityClass(clazz))
            throw new IllegalArgumentException("Not an entity class");
        if(entityDescriptions.containsKey(clazz))
            return entityDescriptions.get(clazz);
        else {
            String internalName = Type.getInternalName(clazz);
            if(preloadEntityDescriptions.containsKey(internalName)) {
                PreloadEntityDescription description = preloadEntityDescriptions.get(internalName);
                EntityDescription entityDescription = description.build(clazz);
                entityDescriptions.put(clazz, entityDescription);
                updateEntityDescription(clazz, entityDescription);
                return entityDescription;
            } else {
                throw new IllegalStateException("Entity " + clazz + " not found!");
            }
        }
    }

    public static boolean isEntityClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }

    private static void updateEntityDescription(Class<?> entityClazz, EntityDescription entityDescription) {
        try {
            Field field = entityClazz.getDeclaredField(EntityClassTransformer.ENTITY_DESCRIPTION_FIELD_NAME);
            field.setAccessible(true);
            field.set(null, entityDescription);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Entity not instrumented!");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}

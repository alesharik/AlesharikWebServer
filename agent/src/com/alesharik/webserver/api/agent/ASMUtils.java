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
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ASMUtils {
    public static boolean hasAnnotation(@Nonnull ClassNode classNode, @Nullable Type annotation) {
        if(annotation == null)
            return true;

        if(classNode.visibleAnnotations != null) {
            for(Object visible : classNode.visibleAnnotations) {
                AnnotationNode node = (AnnotationNode) visible;
                if(annotation.getDescriptor().equals(node.desc))
                    return true;
            }
        }
        return false;
    }

    /**
     * Get annotation parameters from class node
     *
     * @param classNode  the class node
     * @param annotation annotation type
     * @return parameters or null if annotation not found. Parameters will return by name:value pairs. Value can be {@link Object} for objects
     * and {@link java.util.List} with casted primitive for primitives
     */
    @Nullable
    public static Pair<String, Object>[] getAnnotationParams(@Nonnull ClassNode classNode, @Nonnull Type annotation) {
        if(classNode.visibleAnnotations != null) {
            for(Object visible : classNode.visibleAnnotations) {
                AnnotationNode node = (AnnotationNode) visible;
                if(annotation.getDescriptor().equals(node.desc)) {
                    @SuppressWarnings("unchecked") Pair<String, Object>[] ret = new Pair[node.values.size() / 2];
                    int j = 0;
                    for(int i = 0; i < node.values.size(); i += 2) {
                        ret[j] = Pair.of(node.values.get(i).toString(), node.values.get(i + 1));
                        j++;
                    }
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * Get annotation parameters from class node
     *
     * @param classNode  the class node
     * @param annotation annotation type
     * @return parameters or null if annotation not found. Parameters will return by name:value pairs. Value can be {@link Object} for objects
     * and {@link java.util.List} with casted primitive for primitives
     */
    @Nullable
    public static List<Pair<String, Object>[]> getRepetableAnnotationParams(@Nonnull ClassNode classNode, @Nonnull Type annotation) {
        if(classNode.visibleAnnotations != null) {
            List<Pair<String, Object>[]> retList = new ArrayList<>();
            for(Object visible : classNode.visibleAnnotations) {
                AnnotationNode node = (AnnotationNode) visible;
                if(annotation.getDescriptor().equals(node.desc)) {
                    @SuppressWarnings("unchecked") Pair<String, Object>[] ret = new Pair[node.values.size() / 2];
                    int j = 0;
                    for(int i = 0; i < node.values.size(); i += 2) {
                        ret[j] = Pair.of(node.values.get(i).toString(), node.values.get(i + 1));
                        j++;
                    }
                    retList.add(ret);
                }
            }
            if(retList.size() > 0)
                return retList;
        }
        return null;
    }

    /**
     * Return true if opcode store value in local variable
     *
     * @param opcode the opcode
     * @return Return true if opcode store value in local variable, overwise false
     */
    public static boolean isStore(int opcode) {
        return opcode == Opcodes.AASTORE || opcode == Opcodes.ASTORE || opcode == Opcodes.BASTORE || opcode == Opcodes.CASTORE || opcode == Opcodes.DASTORE || opcode == Opcodes.SASTORE || opcode == Opcodes.LSTORE || opcode == Opcodes.LASTORE || opcode == Opcodes.ISTORE || opcode == Opcodes.IASTORE || opcode == Opcodes.FSTORE || opcode == Opcodes.FASTORE || opcode == Opcodes.DSTORE;
    }

    /**
     * Return true if opcode load value from local variable
     *
     * @param opcode the opcode
     * @return Return true if opcode load value from local variable, overwise false
     */
    public static boolean isLoad(int opcode) {
        return opcode == Opcodes.SALOAD || opcode == Opcodes.LALOAD || opcode == Opcodes.IALOAD || opcode == Opcodes.FALOAD || opcode == Opcodes.DALOAD || opcode == Opcodes.CALOAD || opcode == Opcodes.BALOAD || opcode == Opcodes.AALOAD || opcode == Opcodes.LLOAD || opcode == Opcodes.ILOAD || opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD || opcode == Opcodes.ALOAD;
    }
}

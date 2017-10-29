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

import com.alesharik.database.entity.Bridge;
import com.alesharik.database.entity.Column;
import com.alesharik.database.entity.Constraint;
import com.alesharik.database.entity.Creator;
import com.alesharik.database.entity.Destroyer;
import com.alesharik.database.entity.Entity;
import com.alesharik.database.entity.EntityManager;
import com.alesharik.database.entity.ForeignKey;
import com.alesharik.database.entity.Indexed;
import com.alesharik.database.entity.Lazy;
import com.alesharik.database.entity.OverrideDomain;
import com.alesharik.database.entity.PrimaryKey;
import com.alesharik.database.entity.Unique;
import com.alesharik.webserver.api.agent.transformer.ClassTransformer;
import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import com.alesharik.webserver.exceptions.error.UnexpectedBehaviorError;
import com.alesharik.webserver.logger.Debug;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

@UtilityClass
@ClassTransformer
public class EntityClassTransformer {
    public static final String ENTITY_DESCRIPTION_FIELD_NAME = "_entity_description";
    public static final String ENTITY_MANAGER_FIELD_NAME = "_entity_manager";

    private static final String ENTITY_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Entity.class);
    private static final String LAZY_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Lazy.class);
    private static final String BRIDGE_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Bridge.class);

    @TransformAll
    public static byte[] transform(@Param(Param.Type.CLASSFILE_BUFFER) byte[] buffer, @Param(Param.Type.CLASS_NAME) String name) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(buffer);
            classReader.accept(classNode, 0);
            boolean has = false;
            boolean isLazy = false;
            boolean isBridge = false;
            if(classNode.visibleAnnotations != null) {
                for(Object visibleAnnotation : classNode.visibleAnnotations) {
                    if(((AnnotationNode) visibleAnnotation).desc.equals(ENTITY_ANNOTATION_DESCRIPTOR))
                        has = true;
                    else if(((AnnotationNode) visibleAnnotation).desc.equals(LAZY_ANNOTATION_DESCRIPTOR))
                        isLazy = true;
                    else if(((AnnotationNode) visibleAnnotation).desc.equals(BRIDGE_ANNOTATION_DESCRIPTOR))
                        isBridge = true;
                }
            }
            if(!has)
                return null;

            Debug.log("Instrumenting class " + name);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassVisitorImpl cv = new ClassVisitorImpl(classWriter, name, isLazy, isBridge);
            classNode.accept(cv);
            return classWriter.toByteArray();
        } catch (Exception e) {
            if(e.getCause() instanceof ClassNotFoundException) {
                return null;
            }
            System.err.println("Problem in class " + name);
            e.printStackTrace();
        }
        return null;
    }

    private static final class ClassVisitorImpl extends ClassVisitor {
        private static final Type BOOLEAN_OBJECT_TYPE = Type.getType(Boolean.class);
        private static final Type ENTITY_MANAGER_TYPE = Type.getType(EntityManager.class);
        private static final Type ENTITY_DESCRIPTION_TYPE = Type.getType(EntityDescription.class);
        private static final Method ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD;

        static {
            try {
                java.lang.reflect.Method method = EntityManager.class.getDeclaredMethod("getEntityField", Object.class, String.class, EntityDescription.class);
                ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD = Method.getMethod(method);
            } catch (NoSuchMethodException e) {
                throw new UnexpectedBehaviorError("EntityManager.getEntityField method not found! Please, report this to developer!", e);
            }
        }

        private final PreloadEntityDescription entityDescription;
        private final Map<String, Type> fields;

        public ClassVisitorImpl(ClassVisitor cv, String name, boolean lazy, boolean bridge) {
            super(ASM5, cv);
            this.entityDescription = new PreloadEntityDescription(name, lazy, bridge);
            this.fields = new HashMap<>();
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(((access & ACC_TRANSIENT) == ACC_TRANSIENT) || ((access & ACC_STATIC) == ACC_STATIC))
                return super.visitField(access, name, desc, signature, value);
            fields.put(name, Type.getType(desc));
            return new FieldExtractor(super.visitField(access, name, desc, signature, value), entityDescription, (access & ACC_FINAL) == ACC_FINAL, name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if(Modifier.isStatic(access) && contains(Type.getArgumentTypes(desc), Type.getType(EntityManager.class)) && Type.getReturnType(desc).equals(Type.getObjectType(entityDescription.className))) //Most likely factory method
                return new FactoryMethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), entityDescription.className, desc);
            else if(!Modifier.isStatic(access)) { //Can be getter/setter
                if(name.startsWith("set")) {//Is setter
                    String n = name.substring("set".length());
                    String fieldName = Character.toLowerCase(n.charAt(0)) + (n.length() > 1 ? n.substring(1) : "");
                    if(!fields.containsKey(fieldName))
                        return super.visitMethod(access, name, desc, signature, exceptions);
                    return new SetterReplacer(super.visitMethod(access, name, desc, signature, exceptions), fieldName, fields.get(fieldName), entityDescription.className);
                } else if((entityDescription.lazy || entityDescription.bridge) && isGetMethod(name, Type.getReturnType(desc))) { //Getters will be replaced only if entity is lazy or bridge, overwise all values will be fetched form database
                    String fieldName = extractFieldNameFromGetter(name, Type.getReturnType(desc));
                    if(!fields.containsKey(fieldName))
                        return super.visitMethod(access, name, desc, signature, exceptions);
                    if(entityDescription.lazy) {
                        if(fieldName.isEmpty())
                            return super.visitMethod(access, name, desc, signature, exceptions);

                        return new GetterLazyReplacer(super.visitMethod(access, name, desc, signature, exceptions), entityDescription.className, Type.getReturnType(desc), fieldName);
                    } else
                        return new GetterBridgeReplacer(super.visitMethod(access, name, desc, signature, exceptions), entityDescription.className, Type.getReturnType(desc), fieldName);
                } else
                    return new DestroyerMethodTransformer(super.visitMethod(access, name, desc, signature, exceptions), entityDescription.className);
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        private static boolean contains(Type[] types, Type type) {
            for(Type type1 : types) {
                if(type1.equals(type))
                    return true;
            }
            return false;
        }

        private String extractFieldNameFromGetter(String name, Type desc) {
            String s = "";
            if(name.startsWith("get"))
                s = name.substring("get".length());
            else if(isBoolean(desc) && name.startsWith("is"))
                s = name.substring("is".length());
            else
                return "";
            return Character.toLowerCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
        }

        private boolean isGetMethod(String name, Type desc) {
            return name.startsWith("get") || (name.startsWith("is") && isBoolean(desc));
        }

        private boolean isBoolean(Type type) {
            return type.equals(BOOLEAN_OBJECT_TYPE) || type.equals(Type.BOOLEAN_TYPE);
        }

        @Override
        public void visitEnd() {
            visitField(ACC_PRIVATE | ACC_TRANSIENT, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class), null, null);//Create entity manager field. It will be set in Creator method
            visitField(ACC_PRIVATE | ACC_STATIC | ACC_VOLATILE, ENTITY_DESCRIPTION_FIELD_NAME, Type.getDescriptor(EntityDescription.class), null, null);
            EntityClassManager.addPreloadEntityDescription(entityDescription.className, entityDescription);
            super.visitEnd();
        }

        private static final class FieldExtractor extends FieldVisitor {
            private static final String COLUMN_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Column.class);
            private static final String INDEXED_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Indexed.class);
            private static final String PRIMARY_KEY_ANNOTATION_DESCRIPTOR = Type.getDescriptor(PrimaryKey.class);
            private static final String UNIQUE_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Unique.class);
            private static final String FOREIGN_KEY_ANNOTATION_DESCRIPTOR = Type.getDescriptor(ForeignKey.class);
            private static final String NON_NULL_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Nonnull.class);
            private static final String NULLABLE_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Nullable.class);
            private static final String CONSTRAINT_ANNOTATION_DESCRIPTOR = Type.getDescriptor(Constraint.class);
            private static final String OVERRIDE_DOMAIN_ANNOTATION_DESCRIPTOR = Type.getDescriptor(OverrideDomain.class);

            private final PreloadEntityDescription description;
            private final PreloadEntityColumn.PreloadEntityColumnBuilder column;

            public FieldExtractor(FieldVisitor fieldVisitor, PreloadEntityDescription description, boolean isFinal, String fieldName) {
                super(ASM5, fieldVisitor);
                this.description = description;
                this.column = PreloadEntityColumn.builder();
                column.fin(isFinal);
                column.fieldName(fieldName);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if(INDEXED_ANNOTATION_DESCRIPTOR.equals(desc))
                    column.indexed(true);
                else if(PRIMARY_KEY_ANNOTATION_DESCRIPTOR.equals(desc))
                    column.primary(true);
                else if(UNIQUE_ANNOTATION_DESCRIPTOR.equals(desc))
                    column.unique(true);
                else if(NON_NULL_ANNOTATION_DESCRIPTOR.equals(desc))
                    column.nullable(false);
                else if(NULLABLE_ANNOTATION_DESCRIPTOR.equals(desc))
                    column.nullable(true);
                else
                    return new AnnotationValueExtractor(super.visitAnnotation(desc, visible), column, desc);
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                description.columns.add(column.build());
            }

            private static final class AnnotationValueExtractor extends AnnotationVisitor {
                private final PreloadEntityColumn.PreloadEntityColumnBuilder builder;
                private final String desc;

                public AnnotationValueExtractor(AnnotationVisitor av, PreloadEntityColumn.PreloadEntityColumnBuilder builder, String desc) {
                    super(ASM5, av);
                    this.builder = builder;
                    this.desc = desc;
                }

                @Override
                public void visit(String name, Object value) {
                    if(COLUMN_ANNOTATION_DESCRIPTOR.equals(desc))
                        builder.columnName(value.toString());
                    else if(FOREIGN_KEY_ANNOTATION_DESCRIPTOR.equals(desc)) {
                        builder.foreign(true);
                        if("value".equals(name))
                            builder.foreignTable(value.toString());
                        else //columnName
                            builder.foreignColumn(value.toString());
                    } else if(CONSTRAINT_ANNOTATION_DESCRIPTOR.equals(desc)) {
                        if("value".equals(name))
                            builder.constraint(value.toString());
                        else //name
                            builder.constraintName(value.toString());
                    } else if(OVERRIDE_DOMAIN_ANNOTATION_DESCRIPTOR.equals(desc))
                        builder.overrideDomain(value.toString());
                    super.visit(name, value);
                }
            }
        }

        private static final class FactoryMethodTransformer extends MethodVisitor {
            private static final Type FACTORY_METHOD_ANNOTATION_TYPE = Type.getType(Creator.class);

            private static final Method ENTITY_CREATE_METHOD;

            static {
                try {
                    ENTITY_CREATE_METHOD = Method.getMethod(EntityManager.class.getDeclaredMethod("createEntity", Object.class, EntityDescription.class));
                } catch (NoSuchMethodException e) {
                    throw new UnexpectedBehaviorError("EntityManager.createEntity method not found! Please, report this to developer!", e);
                }
            }

            private final String internalName;
            private final Type[] args;
            private volatile boolean ok = false;

            public FactoryMethodTransformer(MethodVisitor mv, String name, String desc) {
                super(ASM5, mv);
                internalName = name;
                args = Type.getArgumentTypes(desc);
            }

            @Override
            public void visitInsn(int opcode) {
                if(opcode == ARETURN && ok) {
                    int paramId = -1;
                    int i = 0;
                    for(Type arg : args) {
                        if(arg.equals(ENTITY_MANAGER_TYPE)) {
                            paramId = i;
                            break;
                        }
                        i++;
                    }
                    if(paramId == -1)
                        throw new IllegalArgumentException("WAT!");

                    visitVarInsn(ALOAD, paramId);//ret var, Entity manager
                    visitInsn(SWAP);//Entity manager, ret var
                    super.visitFieldInsn(GETSTATIC, internalName, ENTITY_DESCRIPTION_FIELD_NAME, ENTITY_DESCRIPTION_TYPE.getDescriptor());//Entity manager, ret var, entity description

                    visitMethodInsn(INVOKEINTERFACE, ENTITY_MANAGER_TYPE.getInternalName(), ENTITY_CREATE_METHOD.getName(), ENTITY_CREATE_METHOD.getDescriptor(), true);//Stack: ret var
                    visitInsn(DUP);//Stack: ret var, ret var
                    visitTypeInsn(CHECKCAST, internalName);
                    visitVarInsn(ALOAD, paramId);//Stack: ret var, ret var, entity manager

                    visitFieldInsn(PUTFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, ENTITY_MANAGER_TYPE.getDescriptor());//Stack: ret var
                    visitTypeInsn(CHECKCAST, internalName);
                    super.visitInsn(ARETURN);//Return
                } else
                    super.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if(Type.getType(desc).equals(FACTORY_METHOD_ANNOTATION_TYPE)) {
                    ok = true;
                }
                return super.visitAnnotation(desc, visible);
            }
        }

        private static final class DestroyerMethodTransformer extends MethodVisitor {
            private static final Type DESTROYER_METHOD_ANNOTATION_TYPE = Type.getType(Destroyer.class);
            private static final Method ENTITY_DELETE_METHOD;

            static {
                try {
                    java.lang.reflect.Method method = EntityManager.class.getDeclaredMethod("deleteEntity", Object.class, EntityDescription.class);
                    ENTITY_DELETE_METHOD = Method.getMethod(method);
                } catch (NoSuchMethodException e) {
                    throw new UnexpectedBehaviorError("EntityManager.deleteEntity method not found! Please, report this to developer!", e);
                }
            }

            private final String internalName;
            private volatile boolean ok = false;

            public DestroyerMethodTransformer(MethodVisitor mv, String internalName) {
                super(ASM5, mv);
                this.internalName = internalName;
            }

            @Override
            public void visitInsn(int opcode) {
                if(opcode == RETURN && ok) {
                    visitVarInsn(ALOAD, 0);
                    visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                    visitVarInsn(ALOAD, 0);
                    visitFieldInsn(GETSTATIC, internalName, ENTITY_DESCRIPTION_FIELD_NAME, ENTITY_DESCRIPTION_TYPE.getDescriptor());

                    visitMethodInsn(INVOKEINTERFACE, ENTITY_MANAGER_TYPE.getInternalName(), ENTITY_DELETE_METHOD.getName(), ENTITY_DELETE_METHOD.getDescriptor(), true);

                    visitVarInsn(ALOAD, 0);
                    visitInsn(ACONST_NULL);
                    visitFieldInsn(PUTFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, ENTITY_MANAGER_TYPE.getDescriptor());
                }
                super.visitInsn(opcode);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if(Type.getType(desc).equals(DESTROYER_METHOD_ANNOTATION_TYPE))
                    ok = true;
                return super.visitAnnotation(desc, visible);
            }
        }

        private static final class GetterLazyReplacer extends MethodVisitor {
            private final String internalName;
            private final Type fieldType;
            private final String fieldName;

            public GetterLazyReplacer(MethodVisitor mv, String internalName, Type fieldType, String fieldName) {
                super(ASM5, mv);
                this.internalName = internalName;
                this.fieldType = fieldType;
                this.fieldName = fieldName;
            }

            @Override
            public void visitCode() {
                Label next1 = new Label();
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                visitJumpInsn(IFNONNULL, next1);
                visitInsn(ACONST_NULL);
                visitInsn(ARETURN);

                visitLabel(next1);

                Label loadedLabel = new Label();
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, fieldName, fieldType.getDescriptor());
                visitJumpInsn(IFNONNULL, loadedLabel);

                visitVarInsn(ALOAD, 0);

                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                visitVarInsn(ALOAD, 0);
                visitLdcInsn(fieldName);
                visitFieldInsn(GETSTATIC, internalName, ENTITY_DESCRIPTION_FIELD_NAME, ENTITY_DESCRIPTION_TYPE.getDescriptor());

                visitMethodInsn(INVOKEINTERFACE, ENTITY_MANAGER_TYPE.getInternalName(), ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD.getName(), ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD.getDescriptor(), true);
                visitTypeInsn(CHECKCAST, fieldType.getInternalName());
                visitInsn(SWAP);
                visitInsn(DUP2);
                visitInsn(SWAP);
                visitFieldInsn(PUTFIELD, internalName, fieldName, fieldType.getDescriptor());
                visitInsn(POP);
                visitInsn(ARETURN);

                visitLabel(loadedLabel);
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, fieldName, fieldType.getDescriptor());
                visitInsn(ARETURN);

                visitEnd();
            }
        }

        private static final class GetterBridgeReplacer extends MethodVisitor {
            private final String internalName;
            private final String fieldName;
            private final Type fieldType;

            public GetterBridgeReplacer(MethodVisitor methodVisitor, String internalName, Type fieldType, String fieldName) {
                super(ASM5, methodVisitor);
                this.internalName = internalName;
                this.fieldName = fieldName;
                this.fieldType = fieldType;
            }

            @Override
            public void visitCode() {
                Label next1 = new Label();
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                visitJumpInsn(IFNONNULL, next1);
                visitInsn(ACONST_NULL);
                visitInsn(ARETURN);

                visitLabel(next1);

                visitVarInsn(ALOAD, 0);

                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                visitVarInsn(ALOAD, 0);
                visitLdcInsn(fieldName);
                visitFieldInsn(GETSTATIC, internalName, ENTITY_DESCRIPTION_FIELD_NAME, ENTITY_DESCRIPTION_TYPE.getDescriptor());

                visitMethodInsn(INVOKEINTERFACE, ENTITY_MANAGER_TYPE.getInternalName(), ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD.getName(), ENTITY_MANAGER_GET_ENTITY_FIELD_METHOD.getDescriptor(), true);
                visitTypeInsn(CHECKCAST, fieldType.getInternalName());
                visitInsn(ARETURN);

                visitEnd();
            }
        }

        private static final class SetterReplacer extends MethodVisitor {
            private final String fieldName;
            private final Type fieldType;
            private final String internalName;

            public SetterReplacer(MethodVisitor mv, String fieldName, Type fieldType, String internalName) {
                super(ASM5, mv);
                this.fieldName = fieldName;
                this.fieldType = fieldType;
                this.internalName = internalName;
            }

            @SneakyThrows
            @Override
            public void visitInsn(int opcode) {
                if(opcode == RETURN) {
                    Label next1 = new Label();
                    visitVarInsn(ALOAD, 0);
                    visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                    visitJumpInsn(IFNONNULL, next1);
                    super.visitInsn(RETURN);

                    visitLabel(next1);
                    visitVarInsn(ALOAD, 0);
                    visitFieldInsn(GETFIELD, internalName, ENTITY_MANAGER_FIELD_NAME, Type.getDescriptor(EntityManager.class));
                    visitVarInsn(ALOAD, 0);//Stack: entity manager, this
                    visitLdcInsn(fieldName);//Stack: entity manager, this, field name
                    visitFieldInsn(GETSTATIC, internalName, ENTITY_DESCRIPTION_FIELD_NAME, Type.getDescriptor(EntityDescription.class));//Stack: entity manager, this, field name, entity description
                    visitVarInsn(ALOAD, 0);
                    visitFieldInsn(GETFIELD, internalName, fieldName, fieldType.getDescriptor());//Stack: entity manager, this, field name, entity description, field current value
                    visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(EntityManager.class), "updateEntity", Type.getMethodDescriptor(EntityManager.class.getDeclaredMethod("updateEntity", Object.class, String.class, EntityDescription.class, Object.class)), true);
                }
                super.visitInsn(opcode);
            }
        }
    }
}

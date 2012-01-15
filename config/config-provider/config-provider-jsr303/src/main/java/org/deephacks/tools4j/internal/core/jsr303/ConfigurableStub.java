/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.tools4j.internal.core.jsr303;

import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.MemberValue;

import javax.validation.Constraint;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class ConfigurableStub {
    private static final String GENERATED_CLASSES = "tools4j_validation_tmp";
    private static final String GENERATED_CLASSES_PREFIX = "runtime_class_registration";
    private CtClass ctClassToGenerate;
    private ClassPool configurableClassPool = ClassPool.getDefault();
    private ClassFile ctConfigurableClassFile;
    private CtClass ctConfigurableClass;
    private HashMap<String, CtField> ctFields = new HashMap<String, CtField>();
    private HashMap<String, FieldWrap<?>> fieldsToGenerate = new HashMap<String, FieldWrap<?>>();
    private File jarFile;
    private File generatedDir;
    private String generatedClassName;
    private ConstPool generatedConstpool;
    private Set<String> annotationDeps = new HashSet<String>();

    public ConfigurableStub(Class<?> configurable,
            Class<? extends Annotation> targetFieldAnnotation, File generatedDir, File jarFile) {
        try {
            this.generatedClassName = configurable.getName();
            this.jarFile = jarFile;
            this.generatedDir = new File(new File(generatedDir, GENERATED_CLASSES),
                    GENERATED_CLASSES_PREFIX);
            this.ctConfigurableClass = configurableClassPool.get(configurable.getName());
            this.ctConfigurableClassFile = ctConfigurableClass.getClassFile();
            try {
                CtClass c = configurableClassPool.get(generatedClassName);
                // class have already been loaded, so defrost to allow modification.
                c.defrost();
            } catch (NotFoundException e) {
                // first time class was loaded, no need to defrost.
            }
            this.ctClassToGenerate = configurableClassPool.makeClass(generatedClassName);

            this.generatedConstpool = ctClassToGenerate.getClassFile().getConstPool();
            Map<String, FieldInfo> configurableFields = getFields(ctConfigurableClassFile);

            ClassIntrospector introspector = new ClassIntrospector(configurable);
            for (FieldWrap<?> configurableField : introspector.getFieldList(targetFieldAnnotation)) {
                CtField ctFieldToGenerate;
                String fieldDeclaration = "public ";
                String fieldType;

                if (configurableField.isCollection()) {
                    fieldType = Collection.class.getName();
                } else if (configurableField.isMap()) {
                    fieldType = Map.class.getName();
                } else {
                    fieldType = configurableField.getType().getName();
                }
                fieldDeclaration = fieldDeclaration + fieldType + " "
                        + configurableField.getFieldName() + ";";
                ctFieldToGenerate = CtField.make(fieldDeclaration, ctClassToGenerate);
                FieldInfo info = configurableFields.get(configurableField.getFieldName());
                addAnnotations(ctFieldToGenerate, info);
                ctClassToGenerate.addField(ctFieldToGenerate);
            }
            for (FieldWrap<?> configurableField : introspector.getFieldList(Id.class)) {
                CtField ctFieldToGenerate = CtField.make(
                        "public " + configurableField.getType().getName() + " "
                                + configurableField.getFieldName() + ";", ctClassToGenerate);
                FieldInfo info = configurableFields.get(configurableField.getFieldName());
                addAnnotations(ctFieldToGenerate, info);
                ctClassToGenerate.addField(ctFieldToGenerate);
            }

            Map<String, MethodInfo> configurableMethods = getMethods(ctConfigurableClassFile);
            for (CtMethod m : ctConfigurableClass.getDeclaredMethods()) {
                MethodInfo sourceInfo = configurableMethods.get(m.getName());
                if (sourceInfo == null) {
                    return;
                }
                CtMethod target = CtNewMethod.copy(m, ctClassToGenerate, null);
                addAnnotations(target, sourceInfo);

                ctClassToGenerate.addMethod(target);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot create stub from [" + configurable + "].", e);
        }
    }

    private Map<String, FieldInfo> getFields(ClassFile classFile) {
        Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
        for (Object fi : classFile.getFields()) {
            FieldInfo fieldInfo = (FieldInfo) fi;
            for (Object attr : fieldInfo.getAttributes()) {
                // do not add methods that arent annotated
                if (!(attr instanceof AnnotationsAttribute)) {
                    continue;
                }
                // index on the name of the field.
                fields.put(fieldInfo.getName(), fieldInfo);
            }
        }

        return fields;
    }

    private Map<String, MethodInfo> getMethods(ClassFile classFile) {
        Map<String, MethodInfo> fields = new HashMap<String, MethodInfo>();
        for (Object mi : classFile.getMethods()) {
            MethodInfo methodInfo = (MethodInfo) mi;
            for (Object attr : methodInfo.getAttributes()) {
                // do not add methods that arent annotated
                if (!(attr instanceof AnnotationsAttribute)) {
                    continue;
                }
                // index on the name of method.
                fields.put(methodInfo.getName(), methodInfo);
            }
        }

        return fields;
    }

    private void addAnnotations(CtField target, FieldInfo source) throws Exception {
        for (Object attr : source.getAttributes()) {
            if (!(attr instanceof AnnotationsAttribute)) {
                continue;
            }
            AnnotationsAttribute annoAttrs = (AnnotationsAttribute) attr;
            AnnotationsAttribute myAttr = new AnnotationsAttribute(generatedConstpool,
                    AnnotationsAttribute.visibleTag);
            for (javassist.bytecode.annotation.Annotation annoAttr : annoAttrs.getAnnotations()) {
                addFieldAnnotation(myAttr, annoAttr, target);
            }
        }
    }

    private void addAnnotations(CtMethod target, MethodInfo source) throws Exception {
        for (Object attr : source.getAttributes()) {
            if (!(attr instanceof AnnotationsAttribute)) {
                continue;
            }

            AnnotationsAttribute annoAttrs = (AnnotationsAttribute) attr;
            /**
             * Wierd that we cant simply copy the attributes.
             * target.getMethodInfo().addAttribute(annoAttrs);
             */
            AnnotationsAttribute myAttr = new AnnotationsAttribute(generatedConstpool,
                    AnnotationsAttribute.visibleTag);
            for (javassist.bytecode.annotation.Annotation annoAttr : annoAttrs.getAnnotations()) {
                addMethodAnnotation(myAttr, annoAttr, target);
            }
        }
    }

    private void addMethodAnnotation(AnnotationsAttribute annoAttrs,
            javassist.bytecode.annotation.Annotation origin, CtMethod ctMethod) throws Exception {
        if (ctMethod == null) {
            return;
        }
        addDependeny(origin);

        Set<?> names = origin.getMemberNames();
        javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(
                origin.getTypeName(), generatedConstpool);
        if (names == null) {
            annoAttrs.addAnnotation(annotation);
            ctMethod.getMethodInfo().addAttribute(annoAttrs);
            return;
        }
        for (Object annoName : names) {
            MemberValue value = origin.getMemberValue(annoName.toString());
            if (value != null) {
                annotation.addMemberValue(annoName.toString(), value);
            }
            annoAttrs.addAnnotation(annotation);

        }
        ctMethod.getMethodInfo().addAttribute(annoAttrs);
    }

    private void addFieldAnnotation(AnnotationsAttribute annoAttrs,
            javassist.bytecode.annotation.Annotation origin, CtField ctField) throws Exception {
        if (ctField == null) {
            return;
        }
        addDependeny(origin);

        Set<?> names = origin.getMemberNames();
        javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation(
                origin.getTypeName(), generatedConstpool);
        if (names == null) {
            annoAttrs.addAnnotation(annotation);
            ctField.getFieldInfo().addAttribute(annoAttrs);
            return;
        }
        for (Object annoName : names) {
            MemberValue value = origin.getMemberValue(annoName.toString());
            if (value != null) {
                annotation.addMemberValue(annoName.toString(), value);
            }
            annoAttrs.addAnnotation(annotation);

        }
        ctField.getFieldInfo().addAttribute(annoAttrs);
    }

    /**
     * Add annotation dependencies to the jar file needed during validation.  
     */
    private void addDependeny(javassist.bytecode.annotation.Annotation origin) {
        String type = origin.getTypeName();
        // filter types which already is already available to the admin class loader.
        if (type.startsWith("java") || Config.class.getName().equals(type)
                || Id.class.getName().equals(type)) {
            return;
        }

        Class<?> annotation = forName(type);
        // traverse the constraint annotation to find custom validators.
        if (annotation.isAnnotationPresent(Constraint.class)) {
            annotationDeps.add(annotation.getName());
            for (Class<?> val : annotation.getAnnotation(Constraint.class).validatedBy()) {
                // TODO: risky, the validator may have runtime dependencies that are missed.
                annotationDeps.add(val.getName());

            }
        }

    }

    public void write() {
        try {
            ctClassToGenerate.writeFile(generatedDir.getAbsolutePath());
            for (String dep : annotationDeps) {

                CtClass c = configurableClassPool.get(dep);
                c.writeFile(generatedDir.getAbsolutePath());
            }
            Archiver.write(generatedDir, jarFile, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot generate [" + ctClassToGenerate + "]", e);
        }
    }

}

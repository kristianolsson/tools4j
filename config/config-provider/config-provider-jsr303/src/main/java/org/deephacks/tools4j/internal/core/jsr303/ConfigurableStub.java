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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.MemberValue;

import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class ConfigurableStub {
    private static final String SUFFIX = "_configurable_stub";
    private static final String GENERATED_CLASSES = "tools4j_validation_tmp";
    private CtClass ctClassToGenerate;
    private ClassPool configurableClassPool = ClassPool.getDefault();
    private ClassFile ctConfigurableClassFile;
    private HashMap<String, CtField> ctFields = new HashMap<String, CtField>();
    private HashMap<String, FieldWrap<?>> fieldsToGenerate = new HashMap<String, FieldWrap<?>>();
    private File jarFile;
    private File generatedDir;
    private String generatedClassName;
    private ConstPool generatedConstpool;

    public ConfigurableStub(Class<?> configurable,
            Class<? extends Annotation> targetFieldAnnotation, File generatedDir, File jarFile) {
        try {
            this.generatedClassName = getClassName(configurable.getName());
            this.jarFile = jarFile;
            this.generatedDir = new File(new File(generatedDir, GENERATED_CLASSES),
                    "runtime_class_registration_" + UUID.randomUUID().toString());
            this.ctConfigurableClassFile = configurableClassPool.get(configurable.getName())
                    .getClassFile();
            try {
                CtClass c = configurableClassPool.get(generatedClassName);
                // class have already been loaded, so defrost to allow modification.
                c.defrost();
            } catch (NotFoundException e) {
                // first time class was loaded, no need to defrost.
            }
            this.ctClassToGenerate = configurableClassPool.makeClass(generatedClassName);

            this.generatedConstpool = ctClassToGenerate.getClassFile().getConstPool();
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
                ctFields.put(configurableField.getFieldName(), ctFieldToGenerate);
                fieldsToGenerate.put(configurableField.getFieldName(), configurableField);
            }
            for (FieldWrap<?> configurableField : introspector.getFieldList(Id.class)) {
                CtField ctFieldToGenerate = CtField.make(
                        "public " + configurableField.getType().getName() + " "
                                + configurableField.getFieldName() + ";", ctClassToGenerate);

                ctFields.put(configurableField.getFieldName(), ctFieldToGenerate);
                fieldsToGenerate.put(configurableField.getFieldName(), configurableField);
            }

            addFieldAnnotations();
            for (CtField f : ctFields.values()) {
                ctClassToGenerate.addField(f);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot create stub from [" + configurable + "].", e);
        }
    }

    public static String getClassName(String fullClassName) {
        return fullClassName + SUFFIX;
    }

    public void addFieldAnnotations() throws Exception {
        for (Object fi : ctConfigurableClassFile.getFields()) {
            FieldInfo fieldInfo = (FieldInfo) fi;
            for (Object attr : fieldInfo.getAttributes()) {
                if (!(attr instanceof AnnotationsAttribute)) {
                    continue;
                }
                AnnotationsAttribute annoAttrs = (AnnotationsAttribute) attr;
                AnnotationsAttribute myAttr = new AnnotationsAttribute(generatedConstpool,
                        AnnotationsAttribute.visibleTag);
                for (javassist.bytecode.annotation.Annotation annoAttr : annoAttrs.getAnnotations()) {
                    CtField ctField = ctFields.get(fieldInfo.getName());
                    addFieldAnnotation(myAttr, annoAttr, ctField);
                }
            }
        }
    }

    private void addFieldAnnotation(AnnotationsAttribute annoAttrs,
            javassist.bytecode.annotation.Annotation origin, CtField ctField) throws Exception {
        if (ctField == null) {
            return;
        }
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

    public void write() {
        try {
            ctClassToGenerate.writeFile(generatedDir.getAbsolutePath());
            Archiver.write(generatedDir, jarFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot generate [" + ctClassToGenerate + "]", e);
        }
    }

    public String getClassName() {
        return generatedClassName;
    }

}

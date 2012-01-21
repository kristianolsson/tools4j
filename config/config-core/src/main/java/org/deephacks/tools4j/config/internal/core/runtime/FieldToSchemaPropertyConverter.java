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
package org.deephacks.tools4j.config.internal.core.runtime;

import static org.deephacks.tools4j.config.model.Events.CFG104_UNSUPPORTED_PROPERTY;
import static org.deephacks.tools4j.config.model.Events.CFG109_ILLEGAL_MAP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema.AbstractSchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.ConversionException;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class FieldToSchemaPropertyConverter implements
        Converter<FieldWrap<Config>, AbstractSchemaProperty> {
    private Conversion conversion = Conversion.get();

    @Override
    public AbstractSchemaProperty convert(FieldWrap<Config> source,
            Class<? extends AbstractSchemaProperty> specificType) {
        if (source.isMap()) {
            List<Class<?>> types = source.getMapParamTypes();
            if (!String.class.equals(types.get(0))) {
                throw CFG109_ILLEGAL_MAP(source.getFieldName());
            }
            if (!types.get(1).isAnnotationPresent(Config.class)) {
                throw CFG109_ILLEGAL_MAP(source.getFieldName());
            }
            return convertReferences(source);
        }
        Class<?> type = source.getType();
        if (type.isAnnotationPresent(Config.class)) {
            return convertReferences(source);
        } else {
            return convertSimple(source);
        }
    }

    private AbstractSchemaProperty convertSimple(FieldWrap<Config> source) {
        String name = source.getAnnotation().name();
        String desc = source.getAnnotation().desc();
        String fieldName = source.getFieldName();
        if (name == null || "".equals(name)) {
            name = fieldName;
        }
        Class<?> type = source.getType();
        validateField(source);
        try {
            if (source.isCollection()) {
                Collection<String> converted = conversion.convert(source.getDefaultValues(),
                        String.class);
                List<String> defaultValues = new ArrayList<String>(converted);

                return SchemaPropertyList.create(name, fieldName, type.getName(), desc, source
                        .isFinal(), source.isEnum(), defaultValues, source.getCollRawType()
                        .getName());
            } else {
                return SchemaProperty.create(name, fieldName, type.getName(), desc,
                        source.isFinal(), source.isEnum(),
                        conversion.convert(source.getDefaultValue(), String.class));
            }
        } catch (ConversionException e) {
            throw CFG104_UNSUPPORTED_PROPERTY(String.class, name, type);
        }
    }

    private AbstractSchemaProperty convertReferences(FieldWrap<Config> source) {
        String name = source.getAnnotation().name();
        String desc = source.getAnnotation().desc();
        String fieldName = source.getFieldName();
        if (name == null || "".equals(name)) {
            name = fieldName;
        }
        Class<?> type = source.getType();
        if (source.isCollection()) {
            return SchemaPropertyRefList.create(name, fieldName, getSchemaName(type), desc,
                    source.isFinal(), source.getCollRawType().getName());
        } else if (source.isMap()) {
            // type is contained in parameterized value of the map
            type = source.getMapParamTypes().get(1);
            return SchemaPropertyRefMap.create(name, fieldName, getSchemaName(type), desc,
                    source.isFinal(), source.getMapRawType().getName());
        } else {
            return SchemaPropertyRef.create(name, fieldName, getSchemaName(type), desc,
                    source.isFinal(), isSingleton(type));
        }
    }

    private String getSchemaName(Class<?> type) {
        Config configurable = type.getAnnotation(Config.class);
        String schemaName = configurable.name();
        if (schemaName == null || "".equals(schemaName)) {
            schemaName = type.getName();
        }
        return schemaName;
    }

    private boolean isSingleton(Class<?> field) {
        ClassIntrospector introspector = new ClassIntrospector(field);
        FieldWrap<Id> id = introspector.getFieldList(Id.class).get(0);
        if (id.isFinal() && id.isStatic()) {
            return true;
        }
        return false;
    }

    private void validateField(FieldWrap<Config> field) {
        if (field.isStatic() && !field.isFinal()) {
            // non-final static @Property not supported.
            throw Events.CFG108_ILLEGAL_MODIFIERS(field.getAnnotation().name());
        }
        if (field.isTransient()) {
            // transient @Property not supported.
            throw Events.CFG108_ILLEGAL_MODIFIERS(field.getAnnotation().name());
        }

    }
}

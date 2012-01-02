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

import static org.deephacks.tools4j.config.Events.CFG104_UNSUPPORTED_PROPERTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Property;
import org.deephacks.tools4j.config.Schema.AbstractSchemaProperty;
import org.deephacks.tools4j.config.Schema.SchemaProperty;
import org.deephacks.tools4j.config.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.ConversionException;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class FieldToSchemaPropertyConverter implements
        Converter<FieldWrap<Property>, AbstractSchemaProperty> {
    private Conversion conversion = Conversion.get();

    @Override
    public AbstractSchemaProperty convert(FieldWrap<Property> source,
            Class<? extends AbstractSchemaProperty> specificType) {
        Class<?> type = source.getType();
        if (type.isAnnotationPresent(Config.class)) {
            return convertReferences(source);
        } else {
            return convertSimple(source);
        }
    }

    private AbstractSchemaProperty convertSimple(FieldWrap<Property> source) {
        String name = source.getAnnotation().name();
        String desc = source.getAnnotation().desc();
        String fieldName = source.getFieldName();
        Class<?> type = source.getType();
        try {
            if (source.isCollection()) {
                Collection<String> converted = conversion.convert(source.getDefaultValues(),
                        String.class);
                List<String> defaultValues = new ArrayList<String>(converted);
                return SchemaPropertyList.create(name, fieldName, type.getName(), desc,
                        defaultValues, source.getCollRawType().getName());
            } else {
                return SchemaProperty.create(name, fieldName, type.getName(), desc,
                        conversion.convert(source.getDefaultValue(), String.class));
            }
        } catch (ConversionException e) {
            throw CFG104_UNSUPPORTED_PROPERTY(String.class, name, type);
        }
    }

    private AbstractSchemaProperty convertReferences(FieldWrap<Property> source) {
        String name = source.getAnnotation().name();
        String desc = source.getAnnotation().desc();
        String fieldName = source.getFieldName();
        Class<?> type = source.getType();

        if (source.isCollection()) {
            return SchemaPropertyRefList.create(name, fieldName, type.getAnnotation(Config.class)
                    .name(), desc, source.getCollRawType().getName());
        } else {
            return SchemaPropertyRef.create(name, fieldName, type.getAnnotation(Config.class)
                    .name(), source.getAnnotation().desc());
        }
    }
}

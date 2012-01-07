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

import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.Property;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.BeanInstance;

public class BeanToObjectConverter implements Converter<Bean, Object> {
    private Conversion conversion = Conversion.get();

    @Override
    public Object convert(Bean source, Class<? extends Object> specificType) {
        BeanInstance<?> instance = BeanInstance.create(specificType);
        Schema schema = source.getSchema();
        Map<String, Object> values = new HashMap<String, Object>();
        for (SchemaProperty prop : schema.get(SchemaProperty.class)) {
            String value = source.getSingleValue(prop.getName());
            String field = prop.getFieldName();
            Object converted = conversion.convert(value, forName(prop.getType()));
            values.put(field, converted);
        }
        for (SchemaPropertyList prop : schema.get(SchemaPropertyList.class)) {
            List<String> vals = source.getValues(prop.getName());
            if (vals == null) {
                continue;
            }
            String field = prop.getFieldName();
            Collection<Object> c = newCollection(forName(prop.getCollectionType()));
            for (String val : vals) {
                Object converted = conversion.convert(val, forName(prop.getType()));
                c.add(converted);
            }

            values.put(field, c);
        }
        for (SchemaPropertyRef prop : schema.get(SchemaPropertyRef.class)) {
            Bean ref = source.getFirstReference(prop.getName()).getBean();
            if (ref == null) {
                continue;
            }
            Schema refSchema = ref.getSchema();
            SchemaPropertyRef schemaRef = schema.get(SchemaPropertyRef.class, prop.getName());
            Object beanInstance = conversion.convert(ref, forName(refSchema.getType()));
            values.put(schemaRef.getFieldName(), beanInstance);

        }
        for (SchemaPropertyRefList prop : schema.get(SchemaPropertyRefList.class)) {
            List<BeanId> beans = source.getReference(prop.getName());
            if (beans == null) {
                continue;
            }
            Collection<Object> c = newCollection(forName(prop.getCollectionType()));
            for (BeanId beanId : beans) {
                Bean b = beanId.getBean();
                Object beanInstance = conversion.convert(b, forName(b.getSchema().getType()));
                c.add(beanInstance);
            }
            values.put(prop.getFieldName(), c);
        }
        if (!schema.getId().isSingleton()) {
            // do not try to inject singleton id: the field is static final
            values.put(getIdField(specificType), source.getId().getInstanceId());
        }
        instance.injectFieldsAnnotatedWith(Property.class).withValues(values);
        instance.injectFieldsAnnotatedWith(Id.class).withValues(values);
        return instance.get();

    }

    private static String getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Annotation annotation = field.getAnnotation(Id.class);
            if (annotation != null) {
                return field.getName();
            }
        }
        throw new RuntimeException("Class [" + clazz + "] does not decalare @Id.");
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollection(Class<?> clazz) {
        if (!clazz.isInterface()) {
            try {
                return (Collection<Object>) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (List.class.isAssignableFrom(clazz)) {
            return new ArrayList<Object>();
        } else if (Set.class.isAssignableFrom(clazz)) {
            return new HashSet<Object>();
        }
        throw new UnsupportedOperationException("Class [" + clazz + "] is not supported.");
    }
}

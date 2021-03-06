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

import static org.deephacks.tools4j.support.reflections.Reflections.findFields;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;
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
        convertProperty(source, schema, values);
        convertPropertyList(source, schema, values);
        convertPropertyRef(source, schema, values);
        convertPropertyRefList(source, schema, values);
        convertPropertyRefMap(source, schema, values);
        if (!schema.getId().isSingleton()) {
            // do not try to inject singleton id: the field is static final
            values.put(getIdField(specificType), source.getId().getInstanceId());
        }
        instance.injectFieldsAnnotatedWith(Config.class).withValues(values);
        instance.injectFieldsAnnotatedWith(Id.class).withValues(values);
        return instance.get();

    }

    private void convertPropertyRefMap(Bean source, Schema schema, Map<String, Object> values) {
        for (SchemaPropertyRefMap prop : schema.get(SchemaPropertyRefMap.class)) {
            List<BeanId> beans = source.getReference(prop.getName());
            if (beans == null) {
                continue;
            }
            Map<Object, Object> c = newMap(forName(prop.getMapType()));
            for (BeanId beanId : beans) {
                Bean b = beanId.getBean();
                if (b != null) {
                    Object beanInstance = conversion.convert(b, forName(b.getSchema().getType()));
                    c.put(beanId.getInstanceId(), beanInstance);
                }
            }
            values.put(prop.getFieldName(), c);
        }
    }

    private void convertPropertyRefList(Bean source, Schema schema, Map<String, Object> values) {
        for (SchemaPropertyRefList prop : schema.get(SchemaPropertyRefList.class)) {
            List<BeanId> beans = source.getReference(prop.getName());
            if (beans == null) {
                continue;
            }
            Collection<Object> c = newCollection(forName(prop.getCollectionType()));
            for (BeanId beanId : beans) {
                Bean b = beanId.getBean();
                if (b != null) {
                    String type = b.getSchema().getType();
                    Object beanInstance = conversion.convert(b, forName(type));
                    c.add(beanInstance);
                }
            }
            values.put(prop.getFieldName(), c);
        }
    }

    private void convertPropertyRef(Bean source, Schema schema, Map<String, Object> values) {
        for (SchemaPropertyRef prop : schema.get(SchemaPropertyRef.class)) {
            BeanId id = source.getFirstReference(prop.getName());
            if (id == null) {
                continue;
            }
            Bean ref = id.getBean();
            if (ref == null) {
                continue;
            }
            Schema refSchema = ref.getSchema();
            SchemaPropertyRef schemaRef = schema.get(SchemaPropertyRef.class, prop.getName());
            Object beanInstance = conversion.convert(ref, forName(refSchema.getType()));
            values.put(schemaRef.getFieldName(), beanInstance);

        }
    }

    private void convertPropertyList(Bean source, Schema schema, Map<String, Object> values) {
        for (SchemaPropertyList prop : schema.get(SchemaPropertyList.class)) {
            List<String> vals = source.getValues(prop.getName());
            String field = prop.getFieldName();

            if (vals == null) {
                continue;
            }
            Collection<Object> c = newCollection(forName(prop.getCollectionType()));
            for (String val : vals) {
                Object converted = conversion.convert(val, forName(prop.getType()));
                c.add(converted);
            }

            values.put(field, c);
        }
    }

    private void convertProperty(Bean source, Schema schema, Map<String, Object> values) {
        for (SchemaProperty prop : schema.get(SchemaProperty.class)) {
            String value = source.getSingleValue(prop.getName());
            String field = prop.getFieldName();
            Object converted = conversion.convert(value, forName(prop.getType()));
            values.put(field, converted);
        }
    }

    private static String getIdField(Class<?> clazz) {
        for (Field field : findFields(clazz)) {
            field.setAccessible(true);
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

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> newMap(Class<?> clazz) {
        if (!clazz.isInterface()) {
            try {
                return (Map<Object, Object>) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return new HashMap<Object, Object>();
        } else if (ConcurrentMap.class.isAssignableFrom(clazz)) {
            return new ConcurrentHashMap<Object, Object>();
        }
        throw new UnsupportedOperationException("Class [" + clazz + "] is not supported.");
    }
}

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
package org.deephacks.tools4j.config;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.config.Bean.BeanId;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class ObjectToBeanConverter implements Converter<Object, Bean> {
    Conversion conversion = Conversion.get();

    @Override
    public Bean convert(Object source, Class<? extends Bean> specificType) {
        ClassIntrospector i = new ClassIntrospector(source.getClass());
        Bean bean = Bean.create(BeanId.create(getId(i, source), i.get(Config.class).name()));

        for (FieldWrap<Property> prop : i.getFieldList(Property.class)) {
            Object value = prop.getValue(source);
            if (value == null) {
                continue;
            }
            addProperty(value, bean, prop);

        }

        Schema schema = conversion.convert(source.getClass(), Schema.class);
        bean.set(schema);

        return bean;
    }

    @SuppressWarnings("unchecked")
    private void addProperty(Object value, Bean bean, FieldWrap<Property> prop) {
        Property property = prop.getAnnotation();
        if (prop.isCollection()) {
            Class<?> paramClass = prop.getType();
            Collection<Object> values = (Collection<Object>) value;
            if (paramClass.isAnnotationPresent(Config.class)) {
                for (Object object : values) {
                    bean.addReference(property.name(), getBeanId(object));
                }
            } else {
                bean.addProperty(property.name(), conversion.convert(values, String.class));
            }
        } else {
            if (value.getClass().isAnnotationPresent(Config.class)) {
                bean.addReference(property.name(), getBeanId(value));
            } else {
                String converted = conversion.convert(value, String.class);
                bean.setProperty(property.name(), converted);
            }
        }
    }

    private static String getId(ClassIntrospector i, Object bean) {
        try {
            List<Field> ids = i.getFieldsAnnotatedWith(Id.class);
            if (ids == null || ids.size() != 1) {
                throw new RuntimeException("Bean does not have @Id annotation " + bean);
            }
            Field beanIdField = ids.get(0);
            return beanIdField.get(bean).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BeanId getBeanId(Object bean) {
        String schemaName = bean.getClass().getAnnotation(Config.class).name();
        String id = getId(new ClassIntrospector(bean.getClass()), bean);
        BeanId targetId = BeanId.create(id, schemaName);
        Bean targetBean = conversion.convert(bean, Bean.class);
        targetBean.set(conversion.convert(bean.getClass(), Schema.class));
        targetId.setBean(targetBean);
        return targetId;
    }
}

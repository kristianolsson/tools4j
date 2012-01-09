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
package org.deephacks.tools4j.config.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class ObjectToBeanConverter implements Converter<Object, Bean> {
    Conversion conversion = Conversion.get();

    @Override
    public Bean convert(Object source, Class<? extends Bean> specificType) {
        ClassIntrospector i = new ClassIntrospector(source.getClass());
        Bean bean = Bean.create(getBeanId(source));

        for (FieldWrap<Config> field : i.getFieldList(Config.class)) {
            Object value = field.getValue(source);
            if (value == null) {
                continue;
            }
            addProperty(value, bean, field);
        }

        Schema schema = conversion.convert(source.getClass(), Schema.class);
        bean.set(schema);
        return bean;
    }

    @SuppressWarnings("unchecked")
    private void addProperty(Object value, Bean bean, FieldWrap<Config> fieldwrap) {
        Config field = fieldwrap.getAnnotation();
        String name = field.name();
        if (name == null || "".equals(name)) {
            name = fieldwrap.getFieldName();
        }
        if (fieldwrap.isCollection()) {
            Class<?> paramClass = fieldwrap.getType();
            Collection<Object> values = (Collection<Object>) value;
            if (paramClass.isAnnotationPresent(Config.class)) {
                for (Object object : values) {
                    bean.addReference(name, getRecursiveBeanId(object));
                }
            } else {
                bean.addProperty(name, conversion.convert(values, String.class));
            }
        } else if (fieldwrap.isMap()) {
            Class<?> paramClass = fieldwrap.getMapParamTypes().get(1);
            Map<String, Object> values = (Map<String, Object>) value;
            if (paramClass.isAnnotationPresent(Config.class)) {
                for (Object object : values.values()) {
                    bean.addReference(name, getRecursiveBeanId(object));
                }
            }
        } else {
            if (value.getClass().isAnnotationPresent(Config.class)) {
                bean.addReference(name, getRecursiveBeanId(value));
            } else {
                String converted = conversion.convert(value, String.class);
                bean.setProperty(name, converted);
            }
        }
    }

    private BeanId getBeanId(Object bean) {
        String schemaName = bean.getClass().getAnnotation(Config.class).name();
        ClassIntrospector i = new ClassIntrospector(bean.getClass());
        List<FieldWrap<Id>> ids = i.getFieldList(Id.class);
        if (ids == null || ids.size() != 1) {
            throw new RuntimeException("Bean does not have @Id annotation " + bean);
        }
        FieldWrap<Id> id = ids.get(0);
        BeanId targetId = null;
        if (id.isFinal() && id.isStatic()) {
            targetId = BeanId.createSingleton(id.getStaticValue().toString(), schemaName);
        } else {
            targetId = BeanId.create(id.getValue(bean).toString(), schemaName);
        }
        return targetId;
    }

    private BeanId getRecursiveBeanId(Object bean) {
        BeanId targetId = getBeanId(bean);
        Bean targetBean = conversion.convert(bean, Bean.class);
        targetBean.set(conversion.convert(bean.getClass(), Schema.class));
        targetId.setBean(targetBean);
        return targetId;
    }
}

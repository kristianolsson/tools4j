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
package org.deephacks.tools4j.support.reflections;

import static org.deephacks.tools4j.support.reflections.Reflections.findFields;
import static org.deephacks.tools4j.support.reflections.Reflections.findFieldsAnnotations;
import static org.deephacks.tools4j.support.reflections.Reflections.newInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class BeanInstance<T> {
    private T instance;
    private Class<T> type;
    private Multimap<Class<? extends Annotation>, Field> fieldAnnotations = ArrayListMultimap
            .create();

    @SuppressWarnings("unchecked")
    private BeanInstance(T instance) {
        this.type = (Class<T>) instance.getClass();
        this.instance = instance;
        List<Field> fields = findFields(type);
        fieldAnnotations = findFieldsAnnotations(fields);
    }

    public T get() {
        return instance;
    }

    public static <T> BeanInstance<T> of(T instance) {
        return new BeanInstance<T>(instance);
    }

    public static <T> Collection<BeanInstance<T>> of(Collection<T> instances) {
        ArrayList<BeanInstance<T>> arrayList = new ArrayList<BeanInstance<T>>();
        for (T instance : instances) {
            arrayList.add(of(instance));
        }
        return arrayList;
    }

    public static <T> BeanInstance<T> create(Class<T> elementType) {
        try {
            return new BeanInstance<T>(newInstance(elementType));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <V extends Annotation> V getClassAnnotation(Class<V> annotation) {
        return type.getAnnotation(annotation);
    }

    public BeanFieldInjector injectFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
        Collection<Field> fields = fieldAnnotations.get(annotation);
        return new BeanFieldInjector(fields, instance, annotation);
    }

    public <V extends Annotation> Map<String, BeanAnnotatedField<V>> findFieldsAnnotatedWith(
            Class<V> annotationClass) {
        Collection<Field> fields = fieldAnnotations.get(annotationClass);
        Map<String, BeanAnnotatedField<V>> map = new HashMap<String, BeanAnnotatedField<V>>();
        for (Field field : fields) {
            map.put(field.getName(),
                    new BeanAnnotatedField<V>(field, field.getAnnotation(annotationClass), instance));
        }
        return map;
    }

    public static class BeanFieldInjector {
        private Collection<Field> fields;
        private Object instance;

        private BeanFieldInjector(Collection<Field> fields, Object instance,
                Class<? extends Annotation> annotation) {
            this.fields = fields;
            this.instance = instance;
        }

        public void withValues(Map<String, Object> values) {
            for (Field field : fields) {
                Object value = values.get(field.getName());
                if (value == null) {
                    continue;
                }
                try {
                    field.set(instance, value);
                } catch (IllegalArgumentException e) {
                    throw new UnsupportedOperationException(e);
                } catch (IllegalAccessException e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        }

        //        private boolean validParameterizedCollectionOrMap(Field field, Object value) {
        //            Class<?> fieldType = field.getType();
        //            if (Collection.class.isAssignableFrom(fieldType)) {
        //
        //                if (!fieldType.isAssignableFrom(value.getClass())) {
        //                    // if the value contains a collections which does not
        //                    // inherit from same base type
        //                    throw new RuntimeException("Data provided to field [" + field
        //                            + "] is of type [" + value.getClass()
        //                            + "] which is not compatible. Data in collection is [" + value + "]");
        //                }
        //                validateCollection(field, value);
        //                // will fail with exception if not valid
        //                return true;
        //
        //            } else if (Map.class.isAssignableFrom(fieldType)) {
        //                validateMap(field, value);
        //                // will fail with exception if not valid
        //                return true;
        //            }
        //            return false;
        //        }
        //
        //        private void validateCollection(Field field, Object value) {
        //            List<Class<?>> parameterizedArguments = GenericReflections.getParameterizedType(field);
        //            if (parameterizedArguments.size() == 0) {
        //                throw new RuntimeException("Collection of field [" + field
        //                        + "] does not have parameterized arguments, which is not allowed.");
        //
        //            }
        //
        //            Class<?> parameterizedArgument = parameterizedArguments.get(0);
        //            Collection<?> collection = (Collection<?>) value;
        //            for (Object object : collection) {
        //                if (!parameterizedArgument.isAssignableFrom(object.getClass())) {
        //                    throw new RuntimeException("Collection to be injected on field [" + field
        //                            + "] has an element [" + object
        //                            + "] which does not match the parameterized type ["
        //                            + parameterizedArgument + "] of the collection.");
        //                }
        //            }
        //        }
        //
        //        private void validateMap(Field field, Object value) {
        //            List<Class<?>> parameterizedArguments = GenericReflections.getParameterizedType(field);
        //            if (parameterizedArguments.size() != 2) {
        //                throw new RuntimeException("Map of field [" + field
        //                        + "] does not have parameterized arguments, which is not allowed.");
        //
        //            }
        //            Class<?> parameterizedKeyArgument = parameterizedArguments.get(0);
        //            Class<?> parameterizedValueArgument = parameterizedArguments.get(1);
        //
        //            Map<?, ?> map = (Map<?, ?>) value;
        //            String errorMsg = "";
        //            for (Object key : map.keySet()) {
        //                if (!parameterizedKeyArgument.isAssignableFrom(key.getClass())) {
        //                    errorMsg = "Map to be injected on field [" + field + "] has an key [" + key
        //                            + "] which does not match the parameterized type ["
        //                            + parameterizedKeyArgument + "] of the map. ";
        //
        //                }
        //
        //                Object keyValue = map.get(key);
        //                if (!parameterizedValueArgument.isAssignableFrom(keyValue.getClass())) {
        //                    errorMsg = errorMsg + "Map to be injected on field [" + field
        //                            + "] has an key [" + key + "] with a value [" + keyValue
        //                            + "] which does not match the parameterized type ["
        //                            + parameterizedValueArgument + "] of the map.";
        //                }
        //                if (!"".equals(errorMsg)) {
        //                    throw new RuntimeException(errorMsg);
        //                }
        //            }
        //        }

    }

}

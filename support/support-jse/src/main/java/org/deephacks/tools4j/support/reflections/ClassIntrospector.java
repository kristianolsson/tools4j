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
import static org.deephacks.tools4j.support.reflections.Reflections.getParameterizedType;
import static org.deephacks.tools4j.support.reflections.Reflections.newInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassIntrospector {
    private Class<?> clazz;
    private List<Field> fields;

    public ClassIntrospector(Class<?> clazz) {
        this.clazz = clazz;
        fields = findFields(clazz);

    }

    /**
     * Get the class name of the class.
     * @return
     */
    public String getName() {
        return clazz.getName();
    }

    public Class<?> getTarget() {
        return clazz;
    }

    /**
     * Get class level annotation for class.
     * 
     * @param annotation
     * @return
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return clazz.getAnnotation(annotation);
    }

    public <T extends Annotation> List<FieldWrap<T>> getFieldList(Class<T> clazz) {
        ArrayList<FieldWrap<T>> wrap = new ArrayList<FieldWrap<T>>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(clazz)) {
                wrap.add(new FieldWrap<T>(f, f.getAnnotation(clazz)));
            }
        }
        return wrap;
    }

    public <T extends Annotation> Map<String, FieldWrap<T>> getFieldMap(Class<T> clazz) {
        HashMap<String, FieldWrap<T>> wrap = new HashMap<String, FieldWrap<T>>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(clazz)) {
                wrap.put(f.getName(), new FieldWrap<T>(f, f.getAnnotation(clazz)));
            }
        }
        return wrap;
    }

    public static class FieldWrap<T extends Annotation> {
        private T annotation;
        private Field field;
        private boolean isCollection = false;
        private boolean isMap = false;
        private Object defaultDeclaringInstance;

        public FieldWrap(Field f, T annotation) {
            this.field = f;
            this.annotation = annotation;
            this.isCollection = Collection.class.isAssignableFrom(f.getType());
            this.isMap = Map.class.isAssignableFrom(f.getType());
        }

        public T getAnnotation() {
            return annotation;
        }

        public String getFieldName() {
            return field.getName();
        }

        public Class<?> getType() {
            if (!isCollection) {
                return field.getType();
            }
            List<Class<?>> p = getParameterizedType(field);
            if (p.size() == 0) {
                throw new UnsupportedOperationException("Collection of field [" + field
                        + "] does not have parameterized arguments, which is not allowed.");
            }
            return p.get(0);
        }

        public List<Class<?>> getMapParamTypes() {
            if (!isMap) {
                throw new UnsupportedOperationException("Field [" + field + "] is not a map.");
            }
            List<Class<?>> p = getParameterizedType(field);
            if (p.size() == 0) {
                throw new UnsupportedOperationException("Map of field [" + field
                        + "] does not have parameterized arguments, which is not allowed.");
            }
            return p;
        }

        public boolean isCollection() {
            return isCollection;
        }

        public boolean isMap() {
            return isMap;
        }

        public boolean isFinal() {
            return Modifier.isFinal(field.getModifiers());
        }

        public boolean isStatic() {
            return Modifier.isStatic(field.getModifiers());
        }

        public boolean isTransient() {
            return Modifier.isTransient(field.getModifiers());
        }

        public boolean isEnum() {
            if (!isCollection) {
                return field.getType().isEnum();
            }
            List<Class<?>> p = getParameterizedType(field);
            if (p.size() == 0) {
                throw new UnsupportedOperationException("Collection of field [" + field
                        + "] does not have parameterized arguments, which is not allowed.");
            }
            return p.get(0).isEnum();

        }

        /**
         * Return the raw collection type
         * @return
         */
        public Class<?> getCollRawType() {
            if (!isCollection) {
                throw new UnsupportedOperationException("This field is not a collection.");
            }
            return field.getType();
        }

        /**
         * Return the raw map type
         * @return
         */
        public Class<?> getMapRawType() {
            if (!isMap) {
                throw new UnsupportedOperationException("This field is not a map.");
            }
            return field.getType();
        }

        public Object getDefaultValue() {
            if (defaultDeclaringInstance == null) {
                try {
                    defaultDeclaringInstance = newInstance(field.getDeclaringClass());
                } catch (InstantiationException e) {
                    throw new UnsupportedOperationException("Cannot access default values "
                            + "from fields of class which cannot be constructed.", e);
                } catch (IllegalAccessException e) {
                    throw new UnsupportedOperationException("Cannot access default values "
                            + "from fields of class which cannot be constructed.", e);
                } catch (InvocationTargetException e) {
                    throw new UnsupportedOperationException("Cannot access default values "
                            + "from fields of class which cannot be constructed.", e);
                } catch (NoSuchMethodException e) {
                    throw new UnsupportedOperationException("Cannot access default values "
                            + "from fields of class which cannot be constructed.", e);
                }
            }
            try {

                return field.get(defaultDeclaringInstance);
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Cannot access default values "
                        + "from fields of instances which cannot be accessed.", e);

            } catch (IllegalAccessException e) {
                throw new UnsupportedOperationException("Cannot access default values "
                        + "from fields of class which cannot be accessed.", e);
            }
        }

        public Object getStaticValue() {
            try {
                return field.get(null);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Cannot access values from fields that arent static.");
            }
        }

        @SuppressWarnings("unchecked")
        public Collection<Object> getDefaultValues() {
            if (!isCollection) {
                throw new UnsupportedOperationException("This field is not a collection.");
            }
            return (Collection<Object>) getDefaultValue();
        }

        public Object getValue(Object source) {
            try {
                return field.get(source);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public <T extends Annotation> List<Field> getFieldsAnnotatedWith(Class<T> clazz) {
        ArrayList<Field> f = new ArrayList<Field>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(clazz)) {
                f.add(field);
            }
        }
        return f;

    }
}

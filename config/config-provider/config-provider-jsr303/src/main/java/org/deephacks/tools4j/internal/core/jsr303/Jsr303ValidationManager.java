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

import static org.deephacks.tools4j.config.model.Events.CFG309_VALIDATION_ERROR;
import static org.deephacks.tools4j.support.reflections.Reflections.findFields;
import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.spi.ClassRepository;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.support.ServiceProvider;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.event.AbortRuntimeException;

@ServiceProvider(service = ValidationManager.class)
public class Jsr303ValidationManager extends ValidationManager {
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private Conversion conversion = Conversion.get();

    @Override
    public void register(String schemaName, Class<?> clazz) throws AbortRuntimeException {
        ClassRepository repos = new ClassRepository();
        repos.add(clazz);
        repos.add(getTransitiveDependencies(clazz));
        repos.write();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void validate(Collection<Bean> beans) throws AbortRuntimeException {
        ClassRepository repos = new ClassRepository();
        ClassLoader cl = repos.getClassLoader();
        ClassLoader org = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            for (Bean bean : beans) {
                Class genclazz = forName(bean.getSchema().getType());
                Object beanToValidate = conversion.convert(bean, genclazz);
                Set<ConstraintViolation<Object>> violations = validator.validate(beanToValidate);
                String msg = "";
                for (ConstraintViolation<Object> v : violations) {
                    msg = msg + v.getPropertyPath() + " " + v.getMessage();
                }
                if (msg != null && !"".equals(msg.trim())) {
                    throw CFG309_VALIDATION_ERROR(msg);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(org);
        }
    }

    private Set<Class<?>> getTransitiveDependencies(Class<?> clazz) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();
        transitive.addAll(getAnnotationDependencies(clazz.getAnnotations()));
        for (Field f : findFields(clazz)) {
            transitive.addAll(getAnnotationDependencies(f.getAnnotations()));
        }
        return transitive;
    }

    private Set<Class<?>> getAnnotationDependencies(Annotation[] annotations) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();
        for (Annotation annonation : annotations) {
            if (annonation.annotationType().isAnnotationPresent(Constraint.class)) {
                transitive.add(annonation.annotationType());
                for (Class<?> dep : annonation.annotationType().getAnnotation(Constraint.class)
                        .validatedBy()) {
                    transitive.add(dep);
                }
            }
        }
        return transitive;
    }

    @Override
    public void unregister(String name) {
        throw new UnsupportedOperationException("FIXME");
    }
}

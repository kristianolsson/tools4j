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
import static org.deephacks.tools4j.support.reflections.Reflections.computeClassHierarchy;
import static org.deephacks.tools4j.support.reflections.Reflections.findFields;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.support.ServiceProvider;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.event.AbortRuntimeException;

@ServiceProvider(service = ValidationManager.class)
public class Jsr303ValidationManager extends ValidationManager {
    public static final String NOT_VALID_TOKEN = "NOT_VALID_TOKEN";
    private static final String GENERATED_CLASSES = "tools4j_validation_tmp";
    private static final String GENERATED_CLASSES_PREFIX = "runtime_class_registration";
    public static final String JSR303_JAR_STORAGE_DIR_PROP = "config.spi.validation.jar.dir";
    private static final SystemProperties PROP = SystemProperties.createDefault();
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static File GENERATED_DIR = getGenerateDir();
    private Conversion conversion = Conversion.get();
    private File jar = new File(GENERATED_DIR, "configurable_stubs.jar");
    private static final Set<String> IGNORED_PACKAGES = new HashSet<String>();

    static {
        IGNORED_PACKAGES.addAll(Arrays.asList("java", Config.class.getName(), Id.class.getName()));
    }

    @Override
    public void register(String schemaName, Class<?> clazz) throws AbortRuntimeException {
        File generatedDir = new File(new File(GENERATED_DIR, GENERATED_CLASSES),
                GENERATED_CLASSES_PREFIX);
        Set<Class<?>> dependencies = new HashSet<Class<?>>();
        dependencies.add(clazz);
        dependencies.addAll(getTransitiveDependencies(clazz));
        Archiver.write(generatedDir, jar, dependencies.toArray(new Class[0]));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void validate(Collection<Bean> beans) throws AbortRuntimeException {

        for (Bean bean : beans) {
            ClassLoader cl = loadJars(jar);
            ClassLoader org = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                Class genclazz;
                String className = bean.getSchema().getType();
                try {
                    genclazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Could not load stub [" + className
                            + "] from jar [" + jar.getAbsolutePath() + "].", e);
                }
                Object beanToValidate = conversion.convert(bean, genclazz);
                Set<ConstraintViolation<Object>> violations = validator.validate(beanToValidate);
                String msg = "";
                for (ConstraintViolation<Object> v : violations) {
                    msg = msg + v.getPropertyPath() + " " + v.getMessage();
                }
                if (msg != null && !"".equals(msg.trim())) {
                    throw CFG309_VALIDATION_ERROR(msg);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(org);
            }
        }
    }

    private Set<Class<?>> getTransitiveDependencies(Class<?> clazz) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();

        transitive.addAll(getSuperClasses(clazz));
        transitive.addAll(getAnnotationDependencies(clazz.getAnnotations()));
        for (Field f : findFields(clazz)) {
            transitive.addAll(getAnnotationDependencies(f.getAnnotations()));
            transitive.addAll(getEnumDependencies(f));
        }
        return transitive;
    }

    private Set<Class<?>> getEnumDependencies(Field f) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();

        if (f.getType().isEnum()) {
            transitive.add(f.getType());
        }
        return transitive;
    }

    private Set<Class<?>> getSuperClasses(Class<?> clazz) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();
        List<Class<?>> classes = computeClassHierarchy(clazz);
        for (Class<?> superclass : classes) {
            if (shouldAdd(superclass)) {
                transitive.add(superclass);
            }
        }
        return transitive;
    }

    private Set<Class<?>> getAnnotationDependencies(Annotation[] annotations) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();
        for (Annotation annonation : annotations) {
            if (annonation.annotationType().isAnnotationPresent(Constraint.class)) {
                if (shouldAdd(annonation.annotationType())) {
                    transitive.add(annonation.annotationType());
                }
                for (Class<?> dep : annonation.annotationType().getAnnotation(Constraint.class)
                        .validatedBy()) {
                    if (shouldAdd(dep)) {
                        transitive.add(dep);
                    }
                }
            }
        }
        return transitive;
    }

    private boolean shouldAdd(Class<?> clazz) {
        String className = clazz.getName();
        for (String ignore : IGNORED_PACKAGES) {
            if (className.startsWith(ignore)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void unregister(String name) {
        throw new UnsupportedOperationException("FIXME");
    }

    private static File getGenerateDir() {
        File generatedDir = null;
        String confDir = PROP.get(JSR303_JAR_STORAGE_DIR_PROP);
        if (confDir == null || "".equals(confDir)) {
            generatedDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            generatedDir = new File(confDir);
        }
        return generatedDir;
    }

    private static ClassLoader loadJars(File... jars) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URLClassLoader jarExtensions = createClassLoaderFrom(jars, cl);
        return jarExtensions;
    }

    private static URLClassLoader createClassLoaderFrom(File[] jars, ClassLoader parent) {
        URL[] urls = toURLs(jars);
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        return classLoader;
    }

    private static URL[] toURLs(File[] files) {
        if (files == null) {
            return new URL[0];
        }
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return urls;
    }

}

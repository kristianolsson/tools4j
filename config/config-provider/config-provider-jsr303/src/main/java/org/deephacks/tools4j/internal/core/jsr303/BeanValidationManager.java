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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import javax.validation.constraints.Size;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.event.AbortRuntimeException;

public class BeanValidationManager extends ValidationManager {
    public static final String JSR303_JAR_STORAGE_DIR_PROP = "config.spi.validation.jar.dir";
    private static final SystemProperties PROP = SystemProperties.createDefault();

    public static void main(String[] args) throws Exception {
        BeanValidationManager manager = new BeanValidationManager();
        manager.register("ConfigTest", ValidateMe.class);
    }

    @Override
    public void register(String schemaName, Class<?> clazz) throws AbortRuntimeException {
        File generatedDir = getGenerateDir();
        File jar = new File(generatedDir, schemaName + ".jar");
        ConfigurableStub stub = new ConfigurableStub(clazz, Config.class, generatedDir, jar);
        stub.write();

        try {
            Class genclazz = loadJars(jar).loadClass(stub.getClassName());
            System.out.println(genclazz);
            for (Field f : genclazz.getDeclaredFields()) {
                for (Annotation a : f.getAnnotations()) {
                    System.out.println("found annotation " + a);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private File getGenerateDir() {
        File generatedDir = null;
        String confDir = PROP.get(JSR303_JAR_STORAGE_DIR_PROP);
        if (confDir == null || "".equals(confDir)) {
            generatedDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            generatedDir = new File(confDir);
        }
        return generatedDir;
    }

    @Config(desc = "")
    public static class MyConfig {
        @Config(desc = "")
        @FirstUpper
        @Size(max = 1)
        private String nammee;
    }

    @Override
    public void validate(Collection<Bean> beans) throws AbortRuntimeException {

    }

    private static ClassLoader loadJars(File... jars) throws IOException {
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

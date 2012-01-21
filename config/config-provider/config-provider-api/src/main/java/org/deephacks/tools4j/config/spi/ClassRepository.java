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
package org.deephacks.tools4j.config.spi;

import static org.deephacks.tools4j.support.reflections.Reflections.computeClassHierarchy;
import static org.deephacks.tools4j.support.reflections.Reflections.findFields;
import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deephacks.tools4j.support.SystemProperties;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ClassRepository {
    private static final String GENERATED_CLASSES = "tools4j_validation_tmp";
    private static final String GENERATED_CLASSES_PREFIX = "runtime_class_registration";
    public static final String CLASS_REPOSITORY_STORAGE_DIR_PROP = "config.spi.class.repository.dir";
    private static final SystemProperties PROP = SystemProperties.createDefault();
    private static File GENERATED_DIR = getGenerateDir();
    private File jar = new File(GENERATED_DIR, "configurable_stubs.jar");
    private File generatedDir = new File(new File(GENERATED_DIR, GENERATED_CLASSES),
            GENERATED_CLASSES_PREFIX);
    private static final Set<String> IGNORED_PACKAGES = new HashSet<String>();
    private Set<Class<?>> dependencies = new HashSet<Class<?>>();

    static {
        IGNORED_PACKAGES.addAll(Arrays.asList("java", "org.deephacks.tools4j.config.spi"));
    }

    public void add(Class<?> clazz) {
        if (shouldAdd(clazz)) {
            dependencies.add(clazz);
        }
        dependencies.addAll(getTransitiveDependencies(clazz));
    }

    public void add(Collection<Class<?>> clazzes) {
        for (Class<?> clazz : clazzes) {
            add(clazz);
        }
    }

    public void write() {
        Archiver.write(generatedDir, jar, dependencies.toArray(new Class[0]));
        dependencies.clear();
    }

    public ClassLoader getClassLoader() {
        return loadJars(jar);
    }

    public Class<?> loadClass(String className) {
        ClassLoader org = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = loadJars(jar);
            Thread.currentThread().setContextClassLoader(cl);
            try {
                return forName(className);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not load stub [" + className
                        + "] from jar [" + jar.getAbsolutePath() + "].", e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(org);
        }
    }

    private static File getGenerateDir() {
        File generatedDir = null;
        String confDir = PROP.get(CLASS_REPOSITORY_STORAGE_DIR_PROP);
        if (confDir == null || "".equals(confDir)) {
            generatedDir = new File(System.getProperty("java.io.tmpdir"));
        } else {
            generatedDir = new File(confDir);
        }
        return generatedDir;
    }

    private Set<Class<?>> getTransitiveDependencies(Class<?> clazz) {
        Set<Class<?>> transitive = new HashSet<Class<?>>();

        transitive.addAll(getSuperClasses(clazz));
        for (Field f : findFields(clazz)) {
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

    private boolean shouldAdd(Class<?> clazz) {
        String className = clazz.getName();
        for (String ignore : IGNORED_PACKAGES) {
            if (className.startsWith(ignore)) {
                return false;
            }
        }
        return true;
    }

    private ClassLoader loadJars(File... jars) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URLClassLoader jarExtensions = createClassLoaderFrom(jars, cl);
        return jarExtensions;
    }

    private URLClassLoader createClassLoaderFrom(File[] jars, ClassLoader parent) {
        URL[] urls = toURLs(jars);
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        return classLoader;
    }

    private URL[] toURLs(File[] files) {
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

    public static class Archiver {

        public static void write(File dir, File jar, Class<?>... clazzes) {
            JavaArchive jarArchieve;
            Map<ArchivePath, Node> contents = null;
            if (jar.exists()) {
                JavaArchive old = ShrinkWrap.createFromZipFile(JavaArchive.class, jar);
                contents = old.getContent();
                jar.delete();
            }
            jarArchieve = ShrinkWrap.create(JavaArchive.class, jar.getName());
            if (contents != null) {
                for (Node n : filter(contents, clazzes)) {

                    jarArchieve.add(n.getAsset(), n.getPath());
                }
            }
            for (Class<?> clazz : clazzes) {
                jarArchieve.addClass(clazz);
            }
            jarArchieve.as(ZipExporter.class).exportTo(jar, true);
        }

        private static Set<Node> filter(Map<ArchivePath, Node> oldNodes, Class<?>... newClasses) {
            Set<Node> nodes = new HashSet<Node>();
            Map<String, Class<?>> classMap = toMap(newClasses);
            for (Node node : oldNodes.values()) {
                if (node.getAsset() == null) {
                    continue;
                }
                String path = node.getPath().get();
                if (path.charAt(0) == File.separatorChar) {
                    path = path.substring(1, path.length());
                }
                if (classMap.get(path) == null) {
                    nodes.add(node);
                }
            }

            return nodes;
        }

        private static Map<String, Class<?>> toMap(Class<?>... clazzes) {
            Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            for (Class<?> clazz : clazzes) {
                String clazzPath = clazz.getName().replace('.', File.separatorChar) + ".class";
                map.put(clazzPath, clazz);
            }
            return map;
        }
    }

}

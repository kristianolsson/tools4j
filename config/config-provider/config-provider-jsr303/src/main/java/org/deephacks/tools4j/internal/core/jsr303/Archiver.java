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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class Archiver {

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

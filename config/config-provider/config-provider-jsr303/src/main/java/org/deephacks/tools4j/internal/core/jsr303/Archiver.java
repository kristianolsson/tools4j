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
import java.util.Map;

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
            jarArchieve = ShrinkWrap.createFromZipFile(JavaArchive.class, jar);
            contents = jarArchieve.getContent();
            jar.delete();
        }
        jarArchieve = ShrinkWrap.create(JavaArchive.class, jar.getName());
        if (contents != null) {
            for (Node n : contents.values()) {
                if (n.getAsset() == null) {
                    continue;
                }
                jarArchieve.add(n.getAsset(), n.getPath());
            }
        }
        for (Class<?> clazz : clazzes) {
            jarArchieve.addClass(clazz);
        }
        jarArchieve.as(ZipExporter.class).exportTo(jar, true);
    }
}

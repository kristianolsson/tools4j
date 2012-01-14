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
import java.io.FilenameFilter;
import java.util.Set;

import org.deephacks.tools4j.support.io.FileUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class Archiver {
    public static void write(File dir, File jar) {
        JavaArchive jarArchieve = ShrinkWrap.create(JavaArchive.class, jar.getName());
        Set<File> classFiles = FileUtils.findFiles(dir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("class");
            }
        });
        for (File classFile : classFiles) {
            String relativeArchiveClass = classFile.getAbsolutePath()
                    .replace(dir.getAbsolutePath(), "").replaceFirst(File.separator, "");
            jarArchieve.add(new FileAsset(classFile), relativeArchiveClass);
        }
        jarArchieve.as(ZipExporter.class).exportTo(jar, true);
    }
}

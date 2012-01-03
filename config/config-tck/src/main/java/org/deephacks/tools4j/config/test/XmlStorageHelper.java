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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.deephacks.tools4j.support.io.FileUtils;
import org.deephacks.tools4j.support.test.JUnitUtils;

public class XmlStorageHelper {

    public static void clearAndInit(Class<?> projectLocalClass) {
        File dir = JUnitUtils.getMavenProjectChildFile(projectLocalClass, "target");
        System.setProperty("config.spi.schema.xml.dir", dir.getAbsolutePath());
        clearXmlStorageFile(dir, "<schema-xml></schema-xml>", "schema.xml");
        System.setProperty("config.spi.bean.xml.dir", dir.getAbsolutePath());
        clearXmlStorageFile(dir, "<bean-xml></bean-xml>", "bean.xml");
    }

    private static void clearXmlStorageFile(File dir, String contents, String fileName) {
        FileWriter writer = null;
        try {
            // make sure dir exist
            if (!dir.exists()) {
                dir.mkdir();

            }
            File file = new File(dir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(new File(dir, fileName));
            writer.write(contents);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.close(writer);
        }
    }

}

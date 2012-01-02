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
package org.deephacks.tools4j.support.test;

import java.io.File;
import java.net.URL;

public class JUnitUtils {
    /**
     * Compute the root directory of this maven project. This will result in the
     * same directory no matter if executed from Eclipse, this maven project root or
     * any parent maven pom directory. 
     * 
     * @param anyTestClass Any test class *local* to the maven project, i.e that 
     * only exist in this maven project.
     * 
     * @param child The file that should be 
     * @return The root directory of this maven project.
     */
    public static File computeMavenProjectRoot(Class<?> anyTestClass) {
        final String clsUri = anyTestClass.getName().replace('.', '/') + ".class";
        final URL url = anyTestClass.getClassLoader().getResource(clsUri);
        final String clsPath = url.getPath();
        // located in ./target/test-classes or ./eclipse-out/target
        final File target_test_classes = new File(clsPath.substring(0,
                clsPath.length() - clsUri.length()));
        // get parent's parent
        return target_test_classes.getParentFile().getParentFile();
    }

    /**
     * Normalizes the root for reading a file to the maven project root directory.
     * 
     * @param anyTestClass Any test class *local* to the maven project, i.e that 
     * only exist in this maven project.
     * 
     * @param child A child path.
     * 
     * @return A file relative to the maven root.
     */
    public static File getMavenProjectChildFile(Class<?> anyTestClass, String child) {
        return new File(computeMavenProjectRoot(anyTestClass), child);
    }

}

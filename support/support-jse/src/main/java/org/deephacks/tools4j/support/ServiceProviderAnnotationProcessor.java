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
package org.deephacks.tools4j.support;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * ServiceProviderAnnotationProcessor is responsible for generating META-INF/services/ files.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("org.deephacks.tools4j.support.ServiceProvider")
public class ServiceProviderAnnotationProcessor extends FileOutputAnnotationProcessor {
    public static final String OUTPUT_DIR = "META-INF/services/";

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        debug("ServiceProviderAnnotationProcessor");
        for (Element implClazz : roundEnv.getElementsAnnotatedWith(ServiceProvider.class)) {
            ServiceProvider provider = implClazz.getAnnotation(ServiceProvider.class);
            try {
                provider.service();
                assert false;
                continue;
            } catch (MirroredTypeException e) {
                debug("Found provider: " + implClazz + " " + e.getTypeMirror());
                // toString on Element results in same as class.getName()
                addLine(implClazz.toString(), getFilePath(e.getTypeMirror()));

            }

        }

        return false;
    }

    /**
     * @param mirror toString on TypeMirror returns the class.getName() 
     * @return
     */
    private String getFilePath(TypeMirror mirror) {
        return OUTPUT_DIR + mirror.toString();
    }
}

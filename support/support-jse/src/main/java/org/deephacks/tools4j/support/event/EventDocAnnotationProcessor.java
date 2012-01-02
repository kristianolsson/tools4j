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
package org.deephacks.tools4j.support.event;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.deephacks.tools4j.support.FileOutputAnnotationProcessor;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("org.deephacks.tools4j.support.event.*")
public class EventDocAnnotationProcessor extends FileOutputAnnotationProcessor {
    public static final String DESC_FILE_SUFFIX = "-events.properties";
    public static final String OUTPUT_DIR = "META-INF/events/";

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(EventDoc.class)) {
            EventDoc event = el.getAnnotation(EventDoc.class);
            debug("Found event: " + event.module() + " " + event.code() + " " + event.desc());
            addLine(getLine(event), getFilePath(event));
        }
        return true;
    }

    private String getLine(EventDoc event) {
        return event.module() + "-" + event.code() + "=" + event.desc();
    }

    private String getFilePath(EventDoc doc) {
        return OUTPUT_DIR + doc.module() + DESC_FILE_SUFFIX;
    }
}

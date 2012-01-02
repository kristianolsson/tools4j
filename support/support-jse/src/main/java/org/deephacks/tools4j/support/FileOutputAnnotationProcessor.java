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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public abstract class FileOutputAnnotationProcessor extends AbstractProcessor {
    Multimap<String, String> fileContents = HashMultimap.create();

    public FileOutputAnnotationProcessor() {
        super();
    }

    public @Override
    final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised()) {
            debug("roundEnv.errorRaised(true)");
            return false;
        }
        if (roundEnv.processingOver()) {
            writeFiles();
            debug("writeFiles()");
            return true;
        } else {
            debug("handleProcess()");
            return handleProcess(annotations, roundEnv);
        }

    }

    protected abstract boolean handleProcess(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv);

    public void addLine(String line, String filePath) {
        fileContents.put(filePath, line);
    }

    private void writeFiles() {
        String file = null;
        try {
            for (String filePath : fileContents.keySet()) {
                debug("writing to file " + filePath);
                file = filePath;
                FileObject out = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT, "", filePath, (Element[]) null);
                OutputStream os = out.openOutputStream();
                try {
                    PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                    for (String line : fileContents.get(filePath)) {
                        w.write(line);
                        w.write("\r\n");
                    }
                    w.flush();
                    w.close();
                } finally {
                    os.close();
                }
            }
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Kind.ERROR,
                    "Failed to write to " + file + ": " + x.toString());
        }
    }

    /**
     * Write a debug statement to a log file. 
     * 
     * @param msg Log file.
     */
    public static void debug(String msg) {
        //        PrintWriter w;
        //        try {
        //            w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(
        //                    "annotation_debug_log.txt"), true), "UTF-8"));
        //            w.write(msg);
        //            w.write("\r\n");
        //            w.flush();
        //            w.close();
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }

    }
}

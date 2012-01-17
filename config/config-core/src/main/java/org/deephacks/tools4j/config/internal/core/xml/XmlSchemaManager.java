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
package org.deephacks.tools4j.config.internal.core.xml;

import static org.deephacks.tools4j.config.model.Events.CFG101_SCHEMA_NOT_EXIST;
import static org.deephacks.tools4j.config.model.Events.CFG202_XML_SCHEMA_FILE_MISSING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaAdapter.XmlSchemas;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.ServiceProvider;
import org.deephacks.tools4j.support.SystemProperties;

@ServiceProvider(service = SchemaManager.class)
public class XmlSchemaManager extends SchemaManager {
    public static final String XML_CONFIG_SCHEMA_FILE_STORAGE_DIR_PROP = "config.spi.schema.xml.dir";
    public static final String XML_SCHEMA_FILE_NAME = "schema.xml";
    private static final SystemProperties PROP = SystemProperties.createDefault();
    private static final long serialVersionUID = 8979172640204086999L;

    @Override
    public Map<String, Schema> getSchemas() {
        return readValues();
    }

    @Override
    public Schema getSchema(String schemaName) {
        Map<String, Schema> values = readValues();
        Schema schema = values.get(schemaName);
        if (schema == null) {
            throw CFG101_SCHEMA_NOT_EXIST(schemaName);
        }
        return schema;
    }

    @Override
    public void regsiterSchema(Schema... schemas) {
        Map<String, Schema> values = readValues();
        for (Schema schema : schemas) {
            values.put(schema.getName(), schema);
            writeValues(values);
        }
    }

    @Override
    public void removeSchema(String schemaName) {
        Map<String, Schema> values = readValues();
        values.remove(schemaName);
        writeValues(values);
    }

    private Map<String, Schema> readValues() {
        String dirValue = PROP.get(XML_CONFIG_SCHEMA_FILE_STORAGE_DIR_PROP);
        if (dirValue == null || "".equals(dirValue)) {
            dirValue = System.getProperty("java.io.tmpdir");
        }
        File file = new File(new File(dirValue), XML_SCHEMA_FILE_NAME);
        if (!file.exists()) {
            throw CFG202_XML_SCHEMA_FILE_MISSING(file);
        }

        try {
            FileInputStream in = new FileInputStream(file);
            JAXBContext context = JAXBContext.newInstance(XmlSchemas.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XmlSchemas schemas = (XmlSchemas) unmarshaller.unmarshal(in);

            return schemas.getSchemas();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw CFG202_XML_SCHEMA_FILE_MISSING(file);
        }

    }

    private void writeValues(Map<String, Schema> values) {
        String dirValue = PROP.get(XML_CONFIG_SCHEMA_FILE_STORAGE_DIR_PROP);
        if (dirValue == null || "".equals(dirValue)) {
            dirValue = System.getProperty("java.io.tmpdir");
        }
        File dir = new File(dirValue);
        if (!dir.exists()) {
            try {
                dir.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File file = new File(dir, XML_SCHEMA_FILE_NAME);
        PrintWriter pw = null;
        try {
            XmlSchemas schemas = new XmlSchemas(values);
            pw = new PrintWriter(file, "UTF-8");
            JAXBContext context = JAXBContext.newInstance(XmlSchemas.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(schemas, pw);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }

    }

}

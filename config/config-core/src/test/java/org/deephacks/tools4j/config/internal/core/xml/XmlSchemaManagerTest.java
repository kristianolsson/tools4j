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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaId;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.junit.Before;
import org.junit.Test;

public class XmlSchemaManagerTest {
    XmlSchemaManager manager = new XmlSchemaManager();

    @Before
    public void before() {
        XmlStorageHelper.clearAndInit(XmlSchemaManagerTest.class);
    }

    @Test
    public void addGetSchema() {
        Set<Schema> schemas = generateSchemas(10, 10);
        for (Schema schema : schemas) {
            manager.regsiterSchema(schema);
            Schema response = manager.getSchema(schema.getName());
            assertThat(schema, equalTo(response));
        }

    }

    @Test
    public void allSchemas() {
        Set<Schema> schemas = generateSchemas(10, 10);
        for (Schema schema : schemas) {
            manager.regsiterSchema(schema);
            Schema response = manager.getSchema(schema.getName());
            assertThat(schema, equalTo(response));
        }

        Map<String, Schema> schemaNames = manager.getSchemas();
        for (Schema s : schemas) {
            assertTrue(schemaNames.containsKey(s.getName()));
        }
    }

    @Test
    public void testRemoveSchema() {
        Set<Schema> schemas = generateSchemas(2, 2);
        for (Schema schema : schemas) {
            manager.regsiterSchema(schema);
            Schema response = manager.getSchema(schema.getName());
            assertThat(schema, equalTo(response));
        }
        Schema s = schemas.iterator().next();
        assertThat(manager.getSchema(s.getName()), is(s));
        manager.removeSchema(s.getName());
        try {
            s = manager.getSchema(s.getName());
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(Events.CFG101));
        }
    }

    public Set<Schema> generateSchemas(int numBeans, int numProps) {
        HashSet<Schema> schemas = new HashSet<Schema>();
        for (int i = 0; i < numBeans; i++) {
            String type = "configType" + i;
            String name = "configName" + i;
            String desc = "configDesc" + i;
            SchemaId id = SchemaId.create("configId" + i, "configDesc" + i, false);
            Schema schema = Schema.create(id, type, name, desc);
            for (int j = 0; j < numProps; j++) {
                String _name = "propName" + j;
                String _fieldName = "propFieldName" + j;
                String _classType = Integer.class.getName();
                String _desc = "propDesc" + j;
                String _defaultValue = "" + j;
                SchemaProperty prop = SchemaProperty.create(_name, _fieldName, _classType, _desc,
                        true, _defaultValue);
                schema.add(prop);
                _name = "collPropName" + j;
                _fieldName = "collpropFieldName" + j;
                _classType = String.class.getName();
                _desc = "collpropDesc" + j;
                _defaultValue = "collpropDefaultValue" + j;
                List<String> _colDefault = new ArrayList<String>();
                _colDefault.add("simple1");
                _colDefault.add("simple2");
                SchemaPropertyList col = SchemaPropertyList.create(_name, _fieldName, _classType,
                        _desc, true, _colDefault, _colDefault.getClass().getName());
                schema.add(col);

            }

            schemas.add(schema);
        }
        return schemas;
    }

}

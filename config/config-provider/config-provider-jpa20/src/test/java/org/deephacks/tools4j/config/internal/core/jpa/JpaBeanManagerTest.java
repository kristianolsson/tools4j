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
package org.deephacks.tools4j.config.internal.core.jpa;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.test.ConfigTestData.Child;
import org.deephacks.tools4j.config.test.ConfigTestData.Grandfather;
import org.deephacks.tools4j.config.test.ConfigTestData.Parent;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.junit.Before;

public class JpaBeanManagerTest {
    RuntimeContext runtime = RuntimeContext.get();

    @Before
    public void before() {
        XmlStorageHelper.clearAndInit(JpaBeanManagerTest.class);
        MockLookup.setMockInstances(BeanManager.class, new Jpa20BeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        runtime.register(Grandfather.class);
        runtime.register(Parent.class);
        runtime.register(Child.class);
    }
}

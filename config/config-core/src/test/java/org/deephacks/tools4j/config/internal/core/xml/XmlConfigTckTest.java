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

import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.config.test.ConfigTckTests;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.internal.core.jsr303.BeanValidationManager;
import org.deephacks.tools4j.support.lookup.MockLookup;

public class XmlConfigTckTest extends ConfigTckTests {

    @Override
    public void before() {
        XmlStorageHelper.clearAndInit(XmlConfigTckTest.class);
        MockLookup.setMockInstances(BeanManager.class, new XmlBeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        MockLookup.addMockInstances(ValidationManager.class, new BeanValidationManager());
    }

}

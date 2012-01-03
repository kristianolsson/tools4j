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
package org.deephacks.tools4j.config.internal.core.runtime;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Arrays;
import java.util.List;

import org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.test.ConfigDefaultSetup;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.config.test.ConfigTestData.Grandfather;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionComparatorMode;

public class RuntimeCoreContextTest extends ConfigDefaultSetup {
    @Before
    public void before() {
        XmlStorageHelper.clearAndInit(RuntimeCoreContextTest.class);
        MockLookup.setMockInstances(BeanManager.class, new XmlBeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        setupDefaultConfigData();

    }

    @Test
    public void testGet() {
        admin.create(defaultBeans);
        Grandfather g_runtime = runtime.get("g1", Grandfather.class);
        assertReflectionEquals(g1, g_runtime, ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void testAll() {
        admin.create(defaultBeans);

        Grandfather g1_runtime = runtime.get("g1", Grandfather.class);
        assertReflectionEquals(g1, g1_runtime, ReflectionComparatorMode.LENIENT_ORDER);

        Grandfather g2_runtime = runtime.get("g2", Grandfather.class);
        assertReflectionEquals(g2, g2_runtime, ReflectionComparatorMode.LENIENT_ORDER);
        List<Grandfather> all = runtime.all(Grandfather.class);

        List<Grandfather> g_list = Arrays.asList(g1, g2);
        assertReflectionEquals(g_list, all, ReflectionComparatorMode.LENIENT_ORDER);
    }

}

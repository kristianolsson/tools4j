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

import static org.deephacks.tools4j.config.model.Events.CFG108;
import static org.deephacks.tools4j.config.model.Events.CFG306;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Arrays;
import java.util.List;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.Multiplicity;
import org.deephacks.tools4j.config.Property;
import org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.test.ConfigDefaultSetup;
import org.deephacks.tools4j.config.test.ConfigTestData.Grandfather;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
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
    public void test_get() {
        admin.create(defaultBeans);
        Grandfather g_runtime = runtime.get("g1", Grandfather.class);
        assertReflectionEquals(g1, g_runtime, ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void test_all() {
        admin.create(defaultBeans);

        Grandfather g1_runtime = runtime.get("g1", Grandfather.class);
        assertReflectionEquals(g1, g1_runtime, ReflectionComparatorMode.LENIENT_ORDER);

        Grandfather g2_runtime = runtime.get("g2", Grandfather.class);
        assertReflectionEquals(g2, g2_runtime, ReflectionComparatorMode.LENIENT_ORDER);
        List<Grandfather> all = runtime.all(Grandfather.class);

        List<Grandfather> g_list = Arrays.asList(g1, g2);
        assertReflectionEquals(g_list, all, ReflectionComparatorMode.LENIENT_ORDER);
    }

    /**
     * Test that final @Property are treated as immutable, that AdminContext should not be able
     * to set it.
     */
    @Test
    public void test_immutable() {
        @Config(name = "immutable", desc = "", multiplicity = Multiplicity.SINGLETON)
        final class ImmutableConfig {
            @Id(name = "", desc = "")
            private String id;

            @Property(name = "test", desc = "")
            private final String test = "test";
        }
        runtime.register(ImmutableConfig.class);
        Bean b = Bean.create(BeanId.create("1", "immutable"));
        b.setProperty("test", "something else");
        try {
            admin.set(b);
            fail("Should not be able to set immutable properties");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG306));
        }
        try {
            admin.create(b);
            fail("Should not be able to set immutable properties");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG306));
        }
        try {
            admin.merge(b);
            fail("Should not be able to set immutable properties");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG306));
        }

    }

    /**
     * Test that transient @Property cannot be registered.
     */
    @Test
    public void test_transient_modifier() {
        @Config(name = "transient", desc = "", multiplicity = Multiplicity.SINGLETON)
        final class TransientConfig {
            @Id(name = "", desc = "")
            private String id;

            @Property(name = "test", desc = "")
            private transient String test = "test";
        }
        try {
            runtime.register(TransientConfig.class);
            fail("Transient properties should not be allowed");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG108));
        }
    }

    /**
     * Test that non-final static @Property cannot be registered.
     */
    @Test
    public void test_non_final_static_modifier() {

        try {
            runtime.register(NonFinalStaticConfig.class);
            fail("Non final static properties should not be allowed");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG108));
        }
    }

    @Config(name = "transient", desc = "", multiplicity = Multiplicity.SINGLETON)
    final static class NonFinalStaticConfig {
        @Id(name = "", desc = "")
        private String id;

        @Property(name = "test", desc = "")
        private static String test = "test";
    }

}

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
package org.deephacks.tools4j.config;

import static org.deephacks.tools4j.config.BeanUnitils.toBean;
import static org.deephacks.tools4j.config.BeanUnitils.toBeans;
import static org.deephacks.tools4j.config.Events.CFG101;
import static org.deephacks.tools4j.config.Events.CFG105;
import static org.deephacks.tools4j.config.Events.CFG106;
import static org.deephacks.tools4j.config.Events.CFG301;
import static org.deephacks.tools4j.config.Events.CFG302;
import static org.deephacks.tools4j.config.Events.CFG304;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.LENIENT_ORDER;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;

import org.deephacks.tools4j.config.Bean.BeanId;
import org.deephacks.tools4j.config.ConfigTestData.Grandfather;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.junit.Before;
import org.junit.Test;

/**
 * A funcational TCK for both runtime and admin external interfaces.
 * 
 * Theses tests are intended to be easily reused as a test suite for simplifying
 * testing compatibility of many different combinations of service providers 
 * and configurations.
 * 
 * It is the responsibility of subclasses to initalize the lookup of
 * service providers and their behaviour.
 * 
 */
public abstract class ConfigFunctionalTests extends ConfigDefaultSetup {
    /**
     * This method can be used to do initalize tests in the subclass 
     * before the superclass.
     */
    public abstract void before();

    @Before
    public final void beforeMethod() {
        before();
        setupDefaultConfigData();
    }

    /**
     * Test the possibility for:
     *  
     * 1) Creating individual beans that have references to eachother.
     * 2) Created beans can be fetched individually.
     * 3) That the runtime view sees the same result.
     */
    @Test
    public void test_create_single_then_get_list() {
        createThenGet(c1);
        createThenGet(c2);
        listAndAssert(c1.getId().getSchemaName(), c1, c2);
        createThenGet(p1);
        createThenGet(p2);
        listAndAssert(p1.getId().getSchemaName(), p1, p2);
        createThenGet(g1);
        createThenGet(g2);
        listAndAssert(g1.getId().getSchemaName(), g1, g2);
    }

    /**
     * Test the possibility for:
     * 
     * 1) Creating a collection of beans that have references to eachother.
     * 2) Created beans can be fetched individually afterwards.
     * 3) Created beans can be listed afterwards.
     * 4) That the runtime view sees the same result as admin view. 
     */
    @Test
    public void test_create_multiple_then_get_list() {
        createDefault();
        getAndAssert(c1);
        getAndAssert(c2);
        listAndAssert(c1.getId().getSchemaName(), c1, c2);
        getAndAssert(p1);
        getAndAssert(p2);
        listAndAssert(p1.getId().getSchemaName(), p1, p2);
        getAndAssert(g1);
        getAndAssert(g2);
        listAndAssert(g1.getId().getSchemaName(), g1, g2);

    }

    /**
     * Test the possibility for:
     * 
     * 1) Setting an empty bean that will erase properties and references.
     * 3) Bean that was set empty can be fetched individually.
     * 4) That the runtime view sees the same result as admin view. 
     */
    @Test
    public void test_set_get_single() {
        createDefault();
        Grandfather empty = testdata.getEmptyGrandfather("g1");
        Bean empty_expect = toBean(empty);

        admin.set(empty_expect);
        Bean empty_result = admin.get(empty.getId());
        assertReflectionEquals(empty_expect, empty_result, LENIENT_ORDER);
    }

    @Test
    public void test_set_get_list() {
        createDefault();
        Grandfather empty_g1 = testdata.getEmptyGrandfather("g1");
        Grandfather empty_g2 = testdata.getEmptyGrandfather("g2");
        Collection<Bean> empty_expect = toBeans(empty_g1, empty_g2);
        admin.set(empty_expect);
        Collection<Bean> empty_result = admin.list(empty_g1.getId().getSchemaName());
        assertReflectionEquals(empty_expect, empty_result, LENIENT_ORDER);
        runtimeAllAndAssert(empty_g1.getClass(), empty_g1, empty_g2);
    }

    @Test
    public void test_merge_get_single() {
        createDefault();

        Grandfather merged = testdata.getEmptyGrandfather("g1");
        merged.prop14 = TimeUnit.NANOSECONDS;
        merged.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
        merged.prop1 = "newName";

        Bean mergeBean = toBean(merged);
        admin.merge(mergeBean);

        // modify the original to fit the expected merge 
        g1.prop1 = merged.prop1;
        g1.prop19 = merged.prop19;
        g1.prop14 = merged.prop14;
        getAndAssert(g1);
    }

    @Test
    public void test_merge_get_list() {
        createDefault();

        Grandfather g1_merged = testdata.getEmptyGrandfather("g1");
        g1_merged.prop14 = TimeUnit.NANOSECONDS;
        g1_merged.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
        g1_merged.prop1 = "newName";

        Grandfather g2_merged = testdata.getEmptyGrandfather("g2");
        g2_merged.prop14 = TimeUnit.NANOSECONDS;
        g2_merged.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
        g2_merged.prop1 = "newName";

        Collection<Bean> mergeBeans = toBeans(g1_merged, g2_merged);
        admin.merge(mergeBeans);

        // modify the original to fit the expected merge 
        g1.prop1 = g1_merged.prop1;
        g1.prop19 = g1_merged.prop19;
        g1.prop14 = g1_merged.prop14;

        g2.prop1 = g2_merged.prop1;
        g2.prop19 = g2_merged.prop19;
        g2.prop14 = g2_merged.prop14;

        listAndAssert(g1.getId().getSchemaName(), g1, g2);

    }

    @Test
    public void test_merge_and_set_broken_references() {

        admin.create(toBeans(g1, g2, p1, p2, c1, c2));

        // try merge a invalid single reference
        Bean b = Bean.create(BeanId.create("p1", ConfigTestData.PARENT_SCHEMA_NAME));
        b.addReference("prop6Name", BeanId.create("non_existing_child_ref", ""));
        try {
            admin.merge(b);
            fail("Should not be possible to merge invalid reference");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG301));
        }

        // try merge a invalid reference on collection
        b = Bean.create(BeanId.create("p2", ConfigTestData.PARENT_SCHEMA_NAME));
        b.addReference("prop7Name", BeanId.create("non_existing_child_ref", ""));
        try {
            admin.merge(b);
            fail("Should not be possible to merge invalid reference");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG301));
        }

        // try set a invalid single reference
        b = Bean.create(BeanId.create("parent4", ConfigTestData.PARENT_SCHEMA_NAME));
        b.addReference("prop6Name", BeanId.create("non_existing_child_ref", ""));
        try {
            admin.set(b);
            fail("Should not be possible to merge beans that does not exist");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG304));
        }

        // try merge a invalid single reference
        b = Bean.create(BeanId.create("p1", ConfigTestData.PARENT_SCHEMA_NAME));
        b.addReference("prop6Name", BeanId.create("non_existing_child_ref", ""));
        try {
            admin.set(b);
            fail("Should not be possible to merge invalid reference");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG301));
        }

    }

    @Test
    public void test_delete_bean() {
        createDefault();

        admin.delete(g1.getId());

        try {
            admin.get(g1.getId());
            fail("Bean should have been deleted");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG304));
        }
    }

    @Test
    public void test_delete_beans() {
        createDefault();

        admin.delete(g1.getId().getSchemaName(), Arrays.asList("g1", "g2"));

        List<Bean> result = admin.list(g1.getId().getSchemaName());
        assertThat(result.size(), is(0));
    }

    @Test
    public void test_delete_reference_violation() {
        admin.create(toBeans(g1, g2, p1, p2, c1, c2));
        // test single
        try {
            admin.delete(BeanId.create("c1", ConfigTestData.CHILD_SCHEMA_NAME));
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG302));
        }
        // test multiple
        try {
            admin.delete(ConfigTestData.CHILD_SCHEMA_NAME, Arrays.asList("c1", "c2"));
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG302));
        }
    }

    @Test
    public void test_set_merge_without_schema() {
        Bean b = Bean.create(BeanId.create("1", "missing_schema_name"));
        try {
            admin.create(b);
            fail("Cant add beans without a schema.");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG101));
        }
        try {
            admin.merge(b);
            fail("Cant add beans without a schema.");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG101));
        }
    }

    @Test
    public void test_set_merge_violating_types() {
        admin.create(toBeans(g1, g2, p1, p2, c1, c2));

        Bean child = Bean.create(BeanId.create("c1", ConfigTestData.CHILD_SCHEMA_NAME));
        // child merge invalid byte
        try {
            child.setProperty("prop8Name", "100000");
            admin.set(child);
            fail("10000 does not fit java.lang.Byte");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG105));
        }
        // child merge invalid integer
        try {
            child.addProperty("prop3Name", "2.2");
            admin.merge(child);
            fail("2.2 does not fit a collection of java.lang.Integer");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG105));
        }
        // parent set invalid enum value 
        Bean parent = Bean.create(BeanId.create("g1", ConfigTestData.GRANDFATHER_SCHEMA_NAME));
        try {
            parent.setProperty("prop14Name", "not_a_enum");
            admin.set(parent);
            fail("not_a_enum is not a value of TimeUnit");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG105));
        }
        // parent merge invalid value to enum list
        parent = Bean.create(BeanId.create("p1", ConfigTestData.PARENT_SCHEMA_NAME));
        try {
            parent.addProperty("prop19Name", "not_a_enum");
            admin.merge(parent);
            fail("not_a_enum is not a value of TimeUnit");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG105));
        }

        // grandfather merge invalid multiplicity type, i.e. single on multi value.
        Bean grandfather = Bean.create(BeanId.create("g1", ConfigTestData.GRANDFATHER_SCHEMA_NAME));
        try {
            grandfather.addProperty("prop1Name", Arrays.asList("1", "2"));
            admin.merge(grandfather);
            fail("Cannot add mutiple values to a single valued property.");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG106));
        }

        // grandfather set invalid multiplicity type, multi value on single.
        grandfather = Bean.create(BeanId.create("p1", ConfigTestData.PARENT_SCHEMA_NAME));
        try {
            grandfather.addProperty("prop11Name", "2.0");
            admin.set(parent);
            fail("Cannot add a value to a single typed value.");
        } catch (AbortRuntimeException e) {
            assertThat(e.getEvent().getCode(), is(CFG105));
        }

    }

    private void createThenGet(Object object) throws AssertionFailedError {
        Bean bean = toBean(object);
        admin.create(bean);
        getAndAssert(object);
    }

    private void getAndAssert(Object object) throws AssertionFailedError {
        Bean bean = toBean(object);
        Bean result = admin.get(bean.getId());
        assertReflectionEquals(bean, result, LENIENT_ORDER);
        runtimeGetAndAssert(object, bean);
    }

    /**
     * Create the default testdata structure. 
     */
    private void createDefault() {
        admin.create(defaultBeans);

    }

    private void listAndAssert(String schemaName, Object... objects) {
        Collection<Bean> beans = admin.list(schemaName);
        assertReflectionEquals(toBeans(objects), beans, LENIENT_ORDER);
        runtimeAllAndAssert(objects[0].getClass(), objects);
    }

    private void runtimeGetAndAssert(Object object, Bean bean) throws AssertionFailedError {
        Object o = runtime.get(bean.getId().getInstanceId(), object.getClass());
        assertReflectionEquals(object, o, LENIENT_ORDER);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void runtimeAllAndAssert(Class clazz, Object... objects) throws AssertionFailedError {
        List<Object> reslut = runtime.all(clazz);
        assertReflectionEquals(objects, reslut, LENIENT_ORDER);
    }
}

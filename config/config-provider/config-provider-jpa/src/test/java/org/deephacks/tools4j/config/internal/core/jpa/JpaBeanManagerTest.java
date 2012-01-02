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

import org.deephacks.tools4j.config.ConfigTestData.Child;
import org.deephacks.tools4j.config.ConfigTestData.Grandfather;
import org.deephacks.tools4j.config.ConfigTestData.Parent;
import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.XmlStorageHelper;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.junit.Before;
import org.junit.Test;

public class JpaBeanManagerTest {
    RuntimeContext runtime = RuntimeContext.get();

    @Before
    public void before() {
        XmlStorageHelper.clearAndInit(JpaBeanManagerTest.class);
        MockLookup.setMockInstances(BeanManager.class, new JpaBeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        runtime.register(Grandfather.class);
        runtime.register(Parent.class);
        runtime.register(Child.class);
    }

    @Test
    public void test() {
        //        ConfigTestData config = new ConfigTestData();
        //        Child c1 = config.getChild("c1");
        //        Child c2 = config.getChild("c2");
        //
        //        Parent p1 = config.getParent("p1");
        //        p1.add(c2, c1);
        //        p1.set(c1);
        //
        //        Parent p2 = config.getParent("p2");
        //        p2.add(c1, c2);
        //        p2.set(c2);
        //
        //        Grandfather g1 = config.getGrandfather("g1");
        //        g1.add(p1, p2);
        //        g1.set(c2);
        //
        //        Grandfather g2 = config.getGrandfather("g2");
        //        g2.add(p1, p2);
        //        g2.set(c2);
        //
        //        //        AdminContext.get().create(BeanUnitils.toBeans(g1, g2, p1, p2, c1, c2));
        //        Bean be = Bean.create(BeanId.create("g1", "GrandfatherSchemaName"));
        //        be.setProperty("prop1Name", "hibernate");
        //        be.setReference("prop7Name", BeanId.create("p2", "ParentSchemaName"));
        //
        //        AdminContext.get().merge(be);

        //        List<Bean> result = AdminContext.get().list("GrandfatherSchemaName");
        //        for (Bean bean : result) {
        //            System.out.println("\n\n" + bean.getId());
        //            for (String n : bean.getPropertyNames()) {
        //                System.out.println(n + "::" + bean.getValues(n));
        //            }
        //            for (BeanId b : bean.getReferences()) {
        //                System.out.println(b + "::" + b.getBean());
        //            }
        //        }
        //        System.out.println("---");
        //        Bean bean = AdminContext.get().list("GrandfatherSchemaName", Arrays.asList("grandfather1"))
        //                .get(0);
        //        System.out.println("\n\n" + bean.getId());
        //        for (String n : bean.getPropertyNames()) {
        //            System.out.println(n + "::" + bean.getValues(n));
        //        }
        //        for (BeanId b : bean.getReferences()) {
        //            System.out.println(b + "::" + b.getBean());
        //        }

    }
}

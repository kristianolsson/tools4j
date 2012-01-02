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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deephacks.tools4j.config.Bean;
import org.deephacks.tools4j.config.Bean.BeanId;
import org.deephacks.tools4j.config.XmlStorageHelper;
import org.junit.Before;
import org.junit.Test;

public class XmlBeanManagerTest {

    private XmlBeanManager manager = new XmlBeanManager();

    @Before
    public void before() {
        XmlStorageHelper.clearAndInit(XmlBeanManagerTest.class);
    }

    @Test
    public void testCreateAll() {
        List<Bean> beans = generateBeans(2, 2);
        manager.create(beans);
        for (Bean b : beans) {
            Bean r = manager.get(b.getId());
            assertThat(r, is(b));
        }

    }

    @Test
    public void testCreateSingle() {
        List<Bean> beans = generateBeans(2, 2);
        for (Bean b : beans) {
            manager.create(b);
        }

        for (Bean b : beans) {
            Bean r = manager.get(b.getId());
            assertThat(r, is(b));
        }
    }

    @Test
    public void testGetEagerly() {
        // add child
        Bean child = Bean.create(BeanId.create("child", "java.lang.String"));
        child.addProperty("property1", "true");
        child.addProperty("property2", "false");
        manager.create(child);

        // add parent that reference child
        Bean parent = Bean.create(BeanId.create("parent", "java.lang.String"));
        parent.addReference("refName", BeanId.create("child", "java.lang.String"));
        parent.addProperty("property1", "prop1");
        parent.addProperty("property2", "prop2");
        manager.create(parent);

        // add grandparent that reference parent
        BeanId grandParentId = BeanId.create("grandparent", "java.lang.String");
        Bean grandparent = Bean.create(grandParentId);
        grandparent.addReference("refName", BeanId.create("parent", "java.lang.String"));
        manager.create(grandparent);

        // query parent and see if bean reference got fetched.
        Bean grandpa = manager.get(grandParentId);

        List<String> childs = grandpa.getReferenceNames();
        assertThat(childs.size(), is(1));
        BeanId childRef = grandpa.getReference(childs.get(0)).get(0);
        Bean childBean = childRef.getBean();
        assertThat(childBean.getId(), is(childBean.getId()));
        assertEquals(childBean.getSingleValue("property1"), "prop1");
        assertEquals(childBean.getSingleValue("property2"), "prop2");

        // parent cool, lets look for children 
        childs = childBean.getReferenceNames();
        assertThat(childs.size(), is(1));
        childRef = childBean.getReference(childs.get(0)).get(0);
        childBean = childRef.getBean();

        assertThat(childBean.getId(), is(childBean.getId()));
        assertEquals(childBean.getSingleValue("property1"), "true");
        assertEquals(childBean.getSingleValue("property2"), "false");
    }

    public List<Bean> generateBeans(int numBeans, int numProps) {
        ArrayList<Bean> beans = new ArrayList<Bean>();
        for (int i = 0; i < numBeans; i++) {
            String id = "beanId" + i;
            String type = "beanType" + i;
            Bean bean = Bean.create(BeanId.create(id, type));
            for (int j = 0; j < numProps; j++) {
                String _name = "propName" + j;
                String _value = "propFieldName" + j;
                bean.addProperty(_name, _value);
                List<String> d = Arrays.asList("1", "2", "3");
                bean.addProperty(_name, d);
            }
            beans.add(bean);
        }
        return beans;
    }

}

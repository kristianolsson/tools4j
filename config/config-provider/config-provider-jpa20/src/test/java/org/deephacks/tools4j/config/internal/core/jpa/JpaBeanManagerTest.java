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

import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.jpa.JpaConfigTckTest.Database;
import org.deephacks.tools4j.config.internal.core.test.Marriage;
import org.deephacks.tools4j.config.internal.core.test.Person;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.web.jpa.EntityManagerFactoryCreator;
import org.deephacks.tools4j.support.web.jpa.ThreadLocalEntityManager;
import org.junit.Before;
import org.junit.Test;

public class JpaBeanManagerTest {

    @Before
    public void before() {

        MockLookup.setMockInstances(BeanManager.class, new Jpa20BeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        RuntimeContext runtime = RuntimeContext.get();
        runtime.register(Marriage.class);
        runtime.register(Person.class);
    }

    @Test
    public void test() {
        new Database("mysql").initalize();
        RuntimeContext runtime = RuntimeContext.get();
        Jpa20BeanManager m = new Jpa20BeanManager();
        EntityManagerFactory factory = EntityManagerFactoryCreator
                .createFactory("tools4j-config-jpa-unit");
        ThreadLocalEntityManager.createEm(factory);
        Bean child1 = createFamily("1", "MALE");
        Bean child2 = createFamily("2", "WOMAN");
        Bean child3 = createFamily("3", "MALE");
        Bean child4 = createFamily("4", "WOMAN");

        Bean child5 = createFamily("1.1", child1, child2, "MALE");
        Bean child6 = createFamily("1.2", child3, child4, "WOMAN");

        Bean child7 = createFamily("1.1.1", child5, child6, "MALE");
        Map<BeanId, Bean> b = m.getBeanToValidate(Bean.create(BeanId.create("1.1.1", "Person")));
        for (Bean bean : b.values()) {
            System.out.println(bean);
        }

        ThreadLocalEntityManager.close();
    }

    public static Bean createFamily(String prefix, String childGender) {
        int counter = 0;
        String lastName = "lastName";
        Bean male = getMale(id(prefix, counter++), "lastName");
        Bean woman = getWoman(id(prefix, counter++), lastName);
        Bean child = getMale(id(prefix, counter++), lastName);
        child.setProperty("gender", childGender);
        Bean marriage = getMarriage(male.getId().getInstanceId(), woman.getId().getInstanceId());
        male.addReference("children", child.getId());
        woman.addReference("children", child.getId());
        marriage.addReference("children", child.getId());
        AdminContext admin = AdminContext.get();
        admin.create(child);
        admin.create(male);
        admin.create(woman);
        admin.create(marriage);

        return child;
    }

    public static Bean createFamily(String prefix, Bean child1, Bean child2, String childGender) {
        AdminContext admin = AdminContext.get();
        int counter = 0;
        String lastName = "lastName";

        Bean male = child1;
        Bean woman = child2;
        Bean child = getMale(id(prefix, counter++), lastName);
        child.setProperty("gender", childGender);
        Bean marriage = getMarriage(male.getId().getInstanceId(), woman.getId().getInstanceId());
        male.addReference("children", child.getId());
        woman.addReference("children", child.getId());

        marriage.addReference("children", child.getId());
        admin.create(child);
        admin.merge(male);
        admin.merge(woman);
        admin.create(marriage);
        return child;
    }

    private static String id(String prefix, int counter) {
        return prefix + "." + ++counter;
    }

    public static Bean getMale(String name, String lastName) {
        Bean b = getPerson(name, lastName);
        b.addProperty("gender", "MALE");
        return b;
    }

    public static Bean getWoman(String name, String lastName) {
        Bean b = getPerson(name, lastName);
        b.addProperty("gender", "WOMAN");
        return b;
    }

    public static Bean getPerson(String name, String lastName) {
        Bean b = Bean.create(BeanId.create(name, "Person"));
        b.addProperty("firstName", name);
        b.addProperty("lastName", lastName);
        return b;
    }

    public static Bean getMarriage(String male, String woman) {
        Bean b = Bean.create(BeanId.create(male + "+" + woman, "Marriage"));
        b.addReference("persons", BeanId.create(male, "Person"));
        b.addReference("persons", BeanId.create(woman, "Person"));
        return b;
    }
}

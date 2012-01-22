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
package org.deephacks.tools4j.config.examples.family;

import static org.deephacks.tools4j.config.examples.family.FamilyTestData.createFamily;
import static org.deephacks.tools4j.support.test.Database.MYSQL;

import java.io.File;

import javax.persistence.EntityManagerFactory;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.jpa.Jpa20BeanManager;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.internal.core.jsr303.Jsr303ValidationManager;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.test.Database;
import org.deephacks.tools4j.support.test.JUnitUtils;
import org.deephacks.tools4j.support.web.jpa.EntityManagerFactoryCreator;
import org.deephacks.tools4j.support.web.jpa.ThreadLocalEntityManager;

/**
 * FamilyTest is dependent on mysql to work correctly.
 */
public class FamilyTest {
    // intentially removed from test suite. Uncomment for demo purposes in eclipse.
    // @Test
    public void passing_test() {
        File scriptDir = JUnitUtils.getMavenProjectChildFile(Jpa20BeanManager.class,
                "src/main/resources/META-INF/");

        Database.create(MYSQL, scriptDir).initalize();
        MockLookup.addMockInstances(ValidationManager.class, new Jsr303ValidationManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        MockLookup.addMockInstances(BeanManager.class, new Jpa20BeanManager());
        AdminContext admin = AdminContext.get();
        EntityManagerFactory factory = EntityManagerFactoryCreator
                .createFactory("tools4j-config-jpa-unit");
        ThreadLocalEntityManager.createEm(factory);
        RuntimeContext.get().register(Person.class, Marriage.class);
        Bean child1 = createFamily("1", "MALE");
        Bean child2 = createFamily("2", "FEMALE");
        Bean child3 = createFamily("3", "MALE");
        Bean child4 = createFamily("4", "FEMALE");

        Bean child5 = createFamily("1.1", child1, child2, "MALE");
        Bean child6 = createFamily("1.2", child3, child4, "FEMALE");

        Bean child7 = createFamily("1.1.1", child5, child6, "MALE");

        ThreadLocalEntityManager.close();

    }
}

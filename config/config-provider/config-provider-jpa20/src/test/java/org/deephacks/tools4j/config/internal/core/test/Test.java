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
package org.deephacks.tools4j.config.internal.core.test;

import javax.persistence.EntityManagerFactory;
import javax.validation.Validation;
import javax.validation.Validator;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.jpa.Jpa20BeanManager;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.internal.core.jsr303.Jsr303ValidationManager;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.web.jpa.EntityManagerFactoryCreator;
import org.deephacks.tools4j.support.web.jpa.ThreadLocalEntityManager;

public class Test {
    public static void main(String[] args) {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        System.out.println(v.validate(new User()));
        MockLookup.addMockInstances(ValidationManager.class, new Jsr303ValidationManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        MockLookup.addMockInstances(BeanManager.class, new Jpa20BeanManager());
        RuntimeContext.get().register(User.class);
        RuntimeContext.get().register(Channel.class);
        Bean b = Bean.create(BeanId.create("as", User.class.getName()));
        EntityManagerFactory factory = EntityManagerFactoryCreator
                .createFactory("tools4j-config-jpa-unit");
        ThreadLocalEntityManager.createEm(factory);
        AdminContext.get().set(b);
        System.out.println("Success");
    }
}

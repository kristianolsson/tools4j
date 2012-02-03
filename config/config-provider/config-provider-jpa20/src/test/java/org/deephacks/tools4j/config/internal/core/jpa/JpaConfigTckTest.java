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

import static org.deephacks.tools4j.support.test.Database.DERBY;
import static org.deephacks.tools4j.support.test.Database.DERBY_DRIVER;
import static org.deephacks.tools4j.support.test.Database.MYSQL;
import static org.deephacks.tools4j.support.test.Database.MYSQL_DRIVER;
import static org.deephacks.tools4j.support.test.Database.POSTGRESQL;
import static org.deephacks.tools4j.support.test.Database.POSTGRESQL_DRIVER;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.config.test.ConfigTckTests;
import org.deephacks.tools4j.config.test.XmlStorageHelper;
import org.deephacks.tools4j.internal.core.jsr303.Jsr303ValidationManager;
import org.deephacks.tools4j.support.io.FileUtils;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.test.Database;
import org.deephacks.tools4j.support.test.EclipseParameterized;
import org.deephacks.tools4j.support.test.JUnitUtils;
import org.deephacks.tools4j.support.web.jpa.EntityManagerFactoryCreator;
import org.deephacks.tools4j.support.web.jpa.ThreadLocalEntityManager;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * Generates a set of test fixtures combinations, each consisting of unique 
 * database and jpa provider configuration for running tck tests targeted 
 * at the JpaBeanManager.
 * 
 * Most tests assume that the database is already installed with a
 * suitable tablespace.
 * 
 * These tests are targeted for a unmanaged Java SE environment.
 * 
 */
@RunWith(value = EclipseParameterized.class)
public class JpaConfigTckTest extends ConfigTckTests {
    public static final String UNIT_NAME = "tools4j-config-jpa-unit";

    /**
     * JPA providers
     */
    public static final String HIBERNATE = "hibernate";
    public static final String ECLIPSELINK = "eclipselink";
    // TO BE IMPLEMENTED LATER
    public static final String OPENJPA = "openjpa";
    public static final String DATANUCLEUS = "datanucleus";
    public static final String OBJECTDB = "objectdb";
    private EntityManagerFactory factory;

    public JpaConfigTckTest(ProviderCombination parameter) {
        if (!parameter.equals(CURRENT_COMBO)) {
            if (factory != null) {
                ThreadLocalEntityManager.close();
                factory.close();
                factory = null;
            }
        }
        CURRENT_COMBO = parameter;
        this.parameter = parameter;

    }

    private static ProviderCombination CURRENT_COMBO;

    /**
     * Clears and initalize the current ProviderCombination consisting of a specific 
     * database and EntityManagerFactory pair. 
     */
    public void before() {
        XmlStorageHelper.clearAndInit(JpaConfigTckTest.class);
        MockLookup.setMockInstances(BeanManager.class, new Jpa20BeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        MockLookup.addMockInstances(ValidationManager.class, new Jsr303ValidationManager());
        File targetDir = JUnitUtils.getMavenProjectChildFile(Jpa20BeanManager.class, "target");
        File jpaProperties = new File(targetDir, "jpa.properties");
        parameter.jpaProvider.write(jpaProperties);
        System.setProperty(EntityManagerFactoryCreator.JPA_PROPERTIES_FILE,
                jpaProperties.getAbsolutePath());
        parameter.database.initalize();
        if (factory == null) {
            factory = EntityManagerFactoryCreator.createFactory(UNIT_NAME);
            ThreadLocalEntityManager.createEm(factory);
        }
    }

    // Unique jpa/database provider combination for a specifci test execution.
    private ProviderCombination parameter;

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> parameters = new ArrayList<Object[]>();
        List<String> dbProviders = Arrays.asList(DERBY);
        List<String> jpaProviders = Arrays.asList(HIBERNATE, ECLIPSELINK);
        List<List<String>> list = new ArrayList<List<String>>();
        list.add(dbProviders);
        list.add(jpaProviders);

        for (List<String> combination : getCombinations(list)) {
            parameters.add(new Object[] { new ProviderCombination(combination.get(0), combination
                    .get(1)) });
        }
        return parameters;
    }

    private static class ProviderCombination {
        private Database database;
        private Jpaprovider jpaProvider;

        public ProviderCombination(String dbProvider, String jpaProvider) {
            File scriptDir = JUnitUtils.getMavenProjectChildFile(Jpa20BeanManager.class,
                    "src/main/resources/META-INF/");
            this.database = Database.create(dbProvider, scriptDir);
            this.jpaProvider = Jpaprovider.create(jpaProvider, database);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProviderCombination other = (ProviderCombination) obj;
            if (database == null) {
                if (other.database != null)
                    return false;
            } else if (!database.equals(other.database))
                return false;
            if (jpaProvider == null) {
                if (other.jpaProvider != null)
                    return false;
            } else if (!jpaProvider.equals(other.jpaProvider))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return database.getDatabaseProvider() + "+" + jpaProvider.getClass().getSimpleName();
        }

    }

    private static <T> List<List<T>> getCombinations(List<List<T>> listOfLists) {
        List<List<T>> returned = new ArrayList<List<T>>();
        if (listOfLists.size() == 1) {
            for (T item : listOfLists.get(0)) {
                List<T> list = new ArrayList<T>();
                list.add(item);
                returned.add(list);
            }
            return returned;
        }
        List<T> itemList = listOfLists.get(0);
        for (List<T> possibleList : getCombinations(listOfLists.subList(1, listOfLists.size()))) {
            for (T item : itemList) {
                List<T> addedList = new ArrayList<T>();
                addedList.add(item);
                addedList.addAll(possibleList);
                returned.add(addedList);
            }
        }
        return returned;
    }

    /**
     * Generates a property file for initalizing the EntityManagerFactory.
     */
    private static abstract class Jpaprovider {
        private static final String PROVIDER = "javax.persistence.provider";
        private static final String URL = "javax.persistence.jdbc.url";
        private static final String DRIVER = "javax.persistence.jdbc.driver";
        private static final String USER = "javax.persistence.jdbc.user";
        private static final String PASSWORD = "javax.persistence.jdbc.password";
        private static final String TRANSACTION_TYPE = "javax.persistence.transactionType";

        protected String provider;
        protected String url;
        protected String driver;
        protected String username;
        protected String password;
        protected String transactionType = "RESOURCE_LOCAL";
        protected HashMap<String, String> providerSpecific = new HashMap<String, String>();

        public Jpaprovider(Database database) {
            url = database.getUrl();
            username = database.getUsername();
            password = database.getPassword();
        }

        public static Jpaprovider create(String jpaProvider, Database database) {
            switch (jpaProvider) {
            case HIBERNATE:
                return new Hibernate(database);
            case ECLIPSELINK:
                return new EclipseLink(database);
            default:
                throw new UnsupportedOperationException();
            }
        }

        public void write(File file) {
            List<String> contents = new ArrayList<String>();
            contents.add(PROVIDER + "=" + provider);
            contents.add(URL + "=" + url);
            contents.add(DRIVER + "=" + driver);
            contents.add(USER + "=" + username);
            contents.add(PASSWORD + "=" + password);
            contents.add(TRANSACTION_TYPE + "=" + transactionType);
            for (String key : providerSpecific.keySet()) {
                contents.add(key + "=" + providerSpecific.get(key));
            }
            FileUtils.writeFile(contents, file);
        }
    }

    private static class Hibernate extends Jpaprovider {
        private static final String DERBY_DIALECT = "org.hibernate.dialect.DerbyDialect";
        private static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";
        private static final String POSTGRESQL_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

        public Hibernate(Database database) {
            super(database);
            provider = "org.hibernate.ejb.HibernatePersistence";

            switch (database.getDatabaseProvider()) {
            case DERBY:
                driver = DERBY_DRIVER;
                providerSpecific.put("hibernate.dialect", DERBY_DIALECT);
                break;
            case MYSQL:
                driver = MYSQL_DRIVER;
                providerSpecific.put("hibernate.dialect", MYSQL_DIALECT);
                break;
            case POSTGRESQL:
                driver = POSTGRESQL_DRIVER;
                providerSpecific.put("hibernate.dialect", POSTGRESQL_DIALECT);
                break;

            default:
                throw new UnsupportedOperationException();
            }
            providerSpecific.put("hibernate.show_sql", "false");
            providerSpecific.put("hibernate.hbm2ddl.auto", "validate");

        }

    }

    private static class EclipseLink extends Jpaprovider {

        public EclipseLink(Database database) {
            super(database);
            provider = "org.eclipse.persistence.jpa.PersistenceProvider";
            switch (database.getDatabaseProvider()) {
            case DERBY:
                driver = DERBY_DRIVER;
                break;
            case MYSQL:
                driver = MYSQL_DRIVER;
                break;
            case POSTGRESQL:
                driver = POSTGRESQL_DRIVER;
                break;
            default:
                throw new UnsupportedOperationException();

            }
            providerSpecific.put("eclipselink.persistence-context.flush-mode", "COMMIT");
        }
    }

}

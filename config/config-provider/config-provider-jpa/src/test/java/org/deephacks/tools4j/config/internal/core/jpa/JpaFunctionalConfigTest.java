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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.deephacks.tools4j.config.ConfigFunctionalTests;
import org.deephacks.tools4j.config.XmlStorageHelper;
import org.deephacks.tools4j.config.internal.core.xml.XmlSchemaManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.io.FileUtils;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.test.EclipseParameterized;
import org.deephacks.tools4j.support.test.JUnitUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

/**
 * Generates a set of test fixtures combinations, each consisting of unique 
 * database and jpa provider configuration for running functional tests targeted 
 * at the JpaBeanManager.
 * 
 * Most tests assume that the database is already installed with a
 * suitable tablespace.
 * 
 * These tests are targeted for a unmanaged Java SE environment.
 * 
 */
@RunWith(value = EclipseParameterized.class)
public class JpaFunctionalConfigTest extends ConfigFunctionalTests {
    /**
     * Database providers
     */
    public static final String MYSQL = "mysql";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public static final String POSTGRESQL = "postgresql";
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";

    // TO BE IMPLEMENTED LATER
    public static final String ORACLE = "oracle";
    public static final String DB2 = "db2";
    public static final String DERBY = "derby";
    public static final String SQLITE = "sqlite";
    public static final String HSQL = "hsql";

    /**
     * JPA providers
     */
    public static final String HIBERNATE = "hibernate";
    public static final String ECLIPSELINK = "eclipselink";
    // TO BE IMPLEMENTED LATER
    public static final String OPENJPA = "openjpa";
    public static final String DATANUCLEUS = "datanucleus";
    public static final String OBJECTDB = "objectdb";

    public JpaFunctionalConfigTest(ProviderCombination parameter) {
        if (!parameter.equals(CURRENT_COMBO)) {
            JpaContextUtils.closeFactory();
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
        XmlStorageHelper.clearAndInit(JpaFunctionalConfigTest.class);
        MockLookup.setMockInstances(BeanManager.class, new JpaBeanManager());
        MockLookup.addMockInstances(SchemaManager.class, new XmlSchemaManager());
        File targetDir = JUnitUtils.getMavenProjectChildFile(JpaBeanManager.class, "target");
        File jpaProperties = new File(targetDir, "jpa.properties");
        parameter.jpaProvider.write(jpaProperties);
        System.setProperty(JpaContextUtils.JPA_PROPERTIES_FILE, jpaProperties.getAbsolutePath());
        parameter.database.initalize();
    }

    // Unique jpa/database provider combination for a specifci test execution.
    private ProviderCombination parameter;

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> parameters = new ArrayList<Object[]>();
        List<String> dbProviders = Arrays.asList(MYSQL);
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
            this.database = Database.create(dbProvider);
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
            return database.dbProvider + "+" + jpaProvider.getClass().getSimpleName();
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
            url = database.url;
            username = database.username;
            password = database.password;
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
        private static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQLDialect";
        private static final String POSTGRESQL_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

        public Hibernate(Database database) {
            super(database);
            provider = "org.hibernate.ejb.HibernatePersistence";

            switch (database.getDatabaseProvider()) {
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

    /**
     * The machine that runs the test must specify username, password and host 
     * for the database. 
     * 
     * Port configuration is not supported at the moment in order to have
     * simple and unified configuration for all databases. 
     */
    private static class Database {
        /**
         * Properties for test databases kept in tools4j for configuration. 
         */
        public static final String DB_HOST_TOOLS4J_CONFIG_PROPERTY = "config.testdb.host";
        /**
         * Install and uninstall database scripts.
         */
        private static final File DB_SCRIPT_DIR = JUnitUtils.getMavenProjectChildFile(
                JpaBeanManager.class, "src/main/resources/META-INF/");
        private static final String INSTALL_DDL = "install_{0}.ddl";
        private static final String UNINSTALL_DDL = "uninstall_{0}.ddl";

        private static final SystemProperties PROPS = SystemProperties.createDefault();

        private String username;
        private String password;
        private String host;
        private String url;
        private String tablespace;
        private String driver;
        private String dbProvider;
        private String installDdl;
        private String uninstallDdl;

        private Database(String dbProvider) {
            this.dbProvider = dbProvider;
            this.installDdl = MessageFormat.format(INSTALL_DDL, dbProvider);
            this.uninstallDdl = MessageFormat.format(UNINSTALL_DDL, dbProvider);
            this.username = System.getProperty("user.name");
            this.password = System.getProperty("user.name");
            this.host = PROPS.get(DB_HOST_TOOLS4J_CONFIG_PROPERTY);
            this.tablespace = System.getProperty("user.name");
            switch (dbProvider) {
            case MYSQL:
                driver = MYSQL_DRIVER;
                url = "jdbc:mysql://" + host + ":3306/" + tablespace;
                break;
            case POSTGRESQL:
                driver = POSTGRESQL_DRIVER;
                url = "jdbc:postgresql://" + host + ":5432/" + tablespace;
                break;
            default:
                throw new UnsupportedOperationException();

            }
        }

        static Database create(String dbProvider) {
            return new Database(dbProvider);
        }

        public String getDatabaseProvider() {
            return this.dbProvider;
        }

        public void initalize() {
            exeucteFile(new File(DB_SCRIPT_DIR, uninstallDdl));
            exeucteFile(new File(DB_SCRIPT_DIR, installDdl));
        }

        private void exeucteFile(File file) {
            SQLExec sql = new SQLExec();
            sql.setProject(new Project());
            sql.setSrc(file);
            sql.setUserid(username);
            sql.setPassword(password);
            sql.setUrl(url);
            sql.setDriver(driver);
            sql.execute();
        }
    }
}

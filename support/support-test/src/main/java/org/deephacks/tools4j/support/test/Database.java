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
package org.deephacks.tools4j.support.test;

import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;

import org.deephacks.tools4j.support.SystemProperties;

/**
 * The machine that runs the test must specify username, password and host 
 * for the database. 
 * 
 * Port configuration is not supported at the moment in order to have
 * simple and unified configuration for all databases. 
 */
public class Database {

    /**
     * Properties for test databases kept in tools4j for configuration. 
     */
    public static final String DB_HOST_TOOLS4J_CONFIG_PROPERTY = "config.testdb.host";

    private static final String INSTALL_DDL = "install_{0}.ddl";
    private static final String UNINSTALL_DDL = "uninstall_{0}.ddl";
    /**
     * Database providers. Derby is default.
     */
    public static final String DERBY = "derby";
    public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    public static final String MYSQL = "mysql";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public static final String POSTGRESQL = "postgresql";
    public static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";

    // TO BE IMPLEMENTED LATER
    public static final String ORACLE = "oracle";
    public static final String DB2 = "db2";

    public static final String SQLITE = "sqlite";
    public static final String HSQL = "hsql";

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
    private File dbScriptDir;

    private Database(String dbProvider, File dbScriptDir) {
        this.dbProvider = dbProvider;
        this.dbScriptDir = dbScriptDir;
        this.installDdl = MessageFormat.format(INSTALL_DDL, dbProvider);
        this.uninstallDdl = MessageFormat.format(UNINSTALL_DDL, dbProvider);
        this.username = System.getProperty("user.name");
        this.password = System.getProperty("user.name");
        this.host = PROPS.get(DB_HOST_TOOLS4J_CONFIG_PROPERTY);
        this.tablespace = System.getProperty("user.name");
        switch (dbProvider) {
        case DERBY:
            driver = DERBY_DRIVER;
            forName(driver);
            url = "jdbc:derby:memory:" + tablespace + ";create=true";
            break;
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

    public static Database create(String dbProvider, File dbScriptDir) {
        return new Database(dbProvider, dbScriptDir);
    }

    public String getDatabaseProvider() {
        return this.dbProvider;
    }

    public void initalize() {
        try {
            if (dbProvider == DERBY) {
                try {
                    /**
                     * Derby is a special case that, at the moment, does not support support "if exist".
                     * The only option is to ignore SQLException from dropping stuff.
                     */
                    DdlExec.execute(new File(dbScriptDir, uninstallDdl), url, username, password,
                            true);
                } catch (SQLException e) {
                    // ignore, probably the first DROP TABLE of a non-existing table
                }
                DdlExec.execute(new File(dbScriptDir, installDdl), url, username, password, false);
            } else {
                DdlExec.execute(new File(dbScriptDir, uninstallDdl), url, username, password, false);
                DdlExec.execute(new File(dbScriptDir, installDdl), url, username, password, false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getUrl() {
        return url;
    }

    public String getTablespace() {
        return tablespace;
    }

    public String getDriver() {
        return driver;
    }

    public String getDbProvider() {
        return dbProvider;
    }

    public File getDbScriptDir() {
        return dbScriptDir;
    }
}

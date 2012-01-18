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

import static com.google.common.io.Files.readLines;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DdlExec {
    public static void execute(File file, String url, String username, String password,
            boolean ignoreSqlEx) throws SQLException, IOException {
        Connection c = getConnection(url, username, password);
        execute(file, c, ignoreSqlEx);
    }

    private static Connection getConnection(String url, String username, String password)
            throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        Connection conn = DriverManager.getConnection(url, connectionProps);
        conn.setAutoCommit(true);
        return conn;
    }

    private static void execute(File f, Connection c, boolean ignoreSqlEx) throws SQLException,
            IOException {
        try {
            try {
                for (String sql : readLines(f, Charset.defaultCharset())) {
                    if (sql == null || "".equals(sql.trim()) || sql.startsWith("--")) {
                        continue;
                    }
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.execute();
                }
            } catch (SQLException e) {
                if (!ignoreSqlEx) {
                    throw e;
                }
            }
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

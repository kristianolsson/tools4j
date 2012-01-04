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
package org.deephacks.tools4j.support.web.jpa;

import static org.deephacks.tools4j.support.web.jpa.JpaEvents.JPA201_PROP_FILEPATH_MISSING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.io.FileUtils;

/**
 * EntityManagerFactoryCreator is responsible for creating an initalizing an EntityManagerFactory
 * in an standalone application-managed environment. 
 */
public class EntityManagerFactoryCreator {
    public static final String PERSISTENCE_UNIT_NAME_PARAM = "persistenceUnitName";
    public static final String JPA_PROPERTIES_FILE = "tools4j.jpa.propfilepath";

    public static final EntityManagerFactory createFactory(String persistenceUnitName) {
        Properties p = new Properties();
        String filePath = SystemProperties.createDefault().get(JPA_PROPERTIES_FILE);
        if (filePath == null || "".equals(filePath)) {
            throw JPA201_PROP_FILEPATH_MISSING(JPA_PROPERTIES_FILE);
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(filePath));
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.close(in);
        }
        return Persistence.createEntityManagerFactory(persistenceUnitName, p);
    }

}

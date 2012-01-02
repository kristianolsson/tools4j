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

import static org.deephacks.tools4j.config.internal.core.jpa.JpaEvents.JPA201_PROP_FILEPATH_MISSING;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Kristoffer Sjogren / ekrisjo
 */
public class JpaContextUtils {
    private static Logger LOG = LoggerFactory.getLogger(JpaContextUtils.class);
    private static final ThreadLocal<EntityManager> CONTEXT = new ThreadLocal<EntityManager>();
    private static final String UNIT_NAME = "tools4j-config-jpa-unit";
    private static EntityManagerFactory FACTORY;
    static final String JPA_PROPERTIES_FILE = "config.spi.bean.jpa.propfilepath";

    private JpaContextUtils() {

    }

    public static EntityManager getEm() {
        initFactory();
        EntityManager manager = CONTEXT.get();

        if (manager == null) {
            manager = FACTORY.createEntityManager();
            manager.getTransaction().begin();
            CONTEXT.set(manager);
        }

        return manager;
    }

    public static void commit() {
        EntityManager manager = CONTEXT.get();
        CONTEXT.set(null);

        if (manager != null) {
            if (manager.isOpen()) {
                manager.getTransaction().commit();
                manager.close();
            }
        } else {
            LOG.warn("EntityManager was already closed.");
        }
    }

    public static void rollback() {
        EntityManager manager = CONTEXT.get();
        CONTEXT.set(null);

        if (manager != null) {
            if (manager.isOpen()) {
                manager.getTransaction().rollback();
                manager.close();
            }
        } else {
            LOG.warn("EntityManager was already closed.");
        }
    }

    private static final void initFactory() {
        if (FACTORY != null) {
            return;
        }
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
        FACTORY = Persistence.createEntityManagerFactory(UNIT_NAME, p);
    }

    public static final void closeFactory() {
        if (FACTORY != null) {
            FACTORY.close();
            FACTORY = null;
        }
    }

}

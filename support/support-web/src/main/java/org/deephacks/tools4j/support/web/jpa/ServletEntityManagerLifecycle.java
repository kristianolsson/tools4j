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

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletEntityManagerLifecycle is responsible for initalizing an EntityManagerFactory
 * and make sure that EntityManagers are create and closed correctly in a servlet context.
 * 
 * This class would only be needed in a pure standalone web container.
 */
public class ServletEntityManagerLifecycle implements ServletContextListener,
        ServletRequestListener {
    public static final String PERSISTENCE_UNIT_NAME_PARAM = "tools4jPersistenceUnitName";
    public static final String JPA_PROPERTIES_FILE = "tools4j.jpa.propfilepath";
    private Logger log = LoggerFactory.getLogger(ServletEntityManagerLifecycle.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sc = event.getServletContext();
        String unitName = sc.getInitParameter(PERSISTENCE_UNIT_NAME_PARAM);
        EntityManagerFactory factory = EntityManagerFactoryCreator.createFactory(unitName);
        event.getServletContext().setAttribute(PERSISTENCE_UNIT_NAME_PARAM, factory);
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        EntityManagerFactory factory = (EntityManagerFactory) event.getServletContext()
                .getAttribute(ServletEntityManagerLifecycle.PERSISTENCE_UNIT_NAME_PARAM);
        ThreadLocalEntityManager.createEm(factory);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ThreadLocalEntityManager.close();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        EntityManagerFactory factory = (EntityManagerFactory) event.getServletContext()
                .getAttribute(PERSISTENCE_UNIT_NAME_PARAM);
        factory.close();
        log.info("EntityManagerFactory [{}] closed.", factory);
    }

}

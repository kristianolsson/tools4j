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

import static org.deephacks.tools4j.support.web.jpa.JpaEvents.JPA202_MISSING_THREAD_EM;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocalEntityManager interact with a thread local in order to retrieve the Entity Manager
 * in a web or standalone context.
 * 
 * The Entity Manager must have been initalized earlier in order for this to work. 
 */
public class ThreadLocalEntityManager {
    private static Logger LOG = LoggerFactory.getLogger(ThreadLocalEntityManager.class);
    private static final ThreadLocal<EntityManager> THREAD_CONTEXT = new ThreadLocal<EntityManager>();

    private ThreadLocalEntityManager() {

    }

    public static void createEm(EntityManagerFactory factory) {
        EntityManager manager = factory.createEntityManager();
        THREAD_CONTEXT.set(manager);
    }

    public static EntityManager getEm() {
        EntityManager manager = THREAD_CONTEXT.get();
        if (manager == null) {
            throw JPA202_MISSING_THREAD_EM();
        }
        return manager;
    }

    public static void begin() {
        EntityManager manager = getEm();
        if (manager == null) {
            LOG.warn("Cannot begin tx, no EntityManager was found in thread local.");
            return;
        }
        if (manager.getTransaction().isActive()) {
            LOG.warn("Cannot begin tx, transaction already active.");
        }
        manager.getTransaction().begin();
    }

    public static void commit() {
        EntityManager manager = THREAD_CONTEXT.get();
        if (manager == null) {
            LOG.warn("Cannot commit, no EntityManager was found in thread local.");
            return;
        }

        if (manager.getTransaction().isActive()) {
            manager.getTransaction().commit();
            manager.clear();
        } else {
            LOG.warn("Cannot rollback tx, no transaction is active.");
        }
    }

    public static void rollback() {
        EntityManager manager = THREAD_CONTEXT.get();
        if (manager == null) {
            LOG.warn("Cannot rollback tx, no EntityManager was found in thread local.");
            return;
        }
        if (manager.getTransaction().isActive()) {
            manager.getTransaction().rollback();
            manager.clear();
        } else {
            LOG.warn("Cannot rollback tx, no transaction is active.");
        }
    }

    public static void close() {
        EntityManager manager = THREAD_CONTEXT.get();
        if (manager == null) {
            LOG.warn("Cannot close, no EntityManager was found in thread local.");
            return;
        }
        THREAD_CONTEXT.set(null);
        if (!manager.isOpen()) {
            LOG.warn("Cannot close, EntityManager has already been closed.");
            return;
        }
        manager.close();
    }

}

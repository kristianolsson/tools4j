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

package org.deephacks.tools4j.config.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Name;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;

/**
 * Maintains a list of Beans affected by operations and coordinates
 * the writing out of changes and the resolution of concurrency
 * concerns.
 * <p>
 * Sessions can tracked persistently, and also commit as part of a JTA
 * transaction, if supported by the Configuration Manager.
 * 
 * This class is not used at the moment, but is kept here for future
 * support.
 * 
 * 
 */
public abstract class BeanSessionManager extends BeanManager {
    private static final long serialVersionUID = 3504689419250093007L;

    /**
     * Commits a unit of work associated with the session towards the
     * underlying storage managed by the bean manager. A new session
     * would need to be created after this method has been called.
     * 
     */
    public void commit() {

    }

    /**
     * Save the session for processing later. The session may or may
     * not be tracked in the underlying storage.
     * 
     * @param timeout when the session will be invalidated and closed.
     *            After timeout, the session cannot be resumed and all
     *            work associated with it will be discarded.
     */
    public abstract void save(long timeout);

    /**
     * Invalidate the ongoing session and discards the unit of work
     * associated with it. A new session would need to be created
     * after this method has been called.
     * 
     */
    public abstract void close();

    /**
     * Return the id associated with the current session. Id can be
     * used to resumed active transactions form the Configuration
     * Manager.
     * 
     * @return Session id
     */
    public abstract String getSessionId();

    /**
     * Resumes an already ongoing session and all work associated with
     * it. This assumes that session bean manager still have an active
     * session with the provided session id. This method can be used
     * for realize long running transactions.
     * 
     * @param sessionId
     * @throws IllegalArgumentException If the session is not valid.
     */
    protected abstract void resumeSession(String sessionId) throws IllegalArgumentException;

    /**
     * Returns a atomic unit of work that have been performed on this
     * bean manager after a certain point in time.
     * 
     * @param afterMs the time in milliseconds after which changes has
     *            been made
     * @param model the model under which changes have been made.
     * @return unit of work that have been performed.
     */
    public abstract UnitOfWork getUnitOfWork(long afterMs, Name model);

    /**
     * Begin a resource local transaction, involving only this bean
     * manager.
     */
    public abstract void begin();

    /**
     * Roll back a resource local transaction, involving only this
     * bean manager.
     */
    public abstract void rollback();

    /**
     * Returns the String that identifies this bean manager. Can be
     * used by the deployment configuration to identify what bean
     * manager implementations shall be bound by which classes.
     * 
     * @return string id.
     */
    public abstract String getId();

    /**
     * Initialize this bean manager with the properties needed for
     * management of a specific storage instance. For example, a
     * deployment configuration may force all models to use same bean
     * manager type, but some models might require them to be stored
     * physical separate locations (different database instances for
     * example).
     * 
     * @param prop used to initialize this bean manager. Null and
     *            empty properties should be supported by the bean
     *            manager, in which case the bean manager should have
     *            discoverable defaults values.
     */
    public abstract void init(Properties prop);

    /**
     * This class is used to track work that has been performed towards a
     * {@link BeanManager}.
     * 
     */
    public static class UnitOfWork {
        private List<BeanId> deleteBeans = new ArrayList<BeanId>();
        private List<Bean> deleteAttributes = new ArrayList<Bean>();
        private List<Bean> mergeBeans = new ArrayList<Bean>();
        private List<Bean> setBeans = new ArrayList<Bean>();
        private long timestamp = 0;

        /**
         * 
         * Creates a new instance of <code>UnitOfWork</code>.
         * 
         * @param timestamp the time after which this unit of work is
         *            applicable.
         */
        public UnitOfWork(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * @return the time after which this unit of work is applicable.
         */
        public long timestamp() {
            return timestamp;
        }

        /**
         * @param beans adds a set operation
         */
        public void addSet(List<Bean> beans) {
            for (Bean bean : beans) {
                addSet(bean);
            }
        }

        /**
         * @param bean add a set operation.
         */
        public void addSet(Bean bean) {
            setBeans.add(bean);
        }

        /**
         * @return atomic operations on this unit of work.
         */
        public List<Bean> set() {
            return setBeans;
        }

        /**
         * @param beans adds a merge operation
         */
        public void addMerge(List<Bean> beans) {
            for (Bean bean : beans) {
                addMerge(bean);
            }
        }

        /**
         * @param bean add a merge operation
         */
        public void addMerge(Bean bean) {
            mergeBeans.add(bean);
        }

        /**
         * @return get merge operations.
         */
        public List<Bean> merge() {
            return mergeBeans;
        }

        /**
         * @param names add delete operation
         */
        public void addDelete(List<BeanId> ids) {
            for (BeanId id : ids) {
                addDelete(id);
            }
        }

        /**
         * @param name add delete operation
         */
        public void addDelete(BeanId id) {
            deleteBeans.add(id);
        }

        /**
         * @return get delete operations
         */
        public List<BeanId> delete() {
            return deleteBeans;
        }

        /**
         * @param beans add delete attribute operations.
         */
        public void addDeleteProperty(List<Bean> beans) {
            for (Bean bean : beans) {
                addDeleteProperty(bean);
            }
        }

        /**
         * @param bean add delete property operation.
         */
        public void addDeleteProperty(Bean bean) {
            deleteAttributes.add(bean);
        }

        /**
         * @return get delete property operations.
         */
        public List<Bean> deleteProperty() {
            return deleteAttributes;
        }

    }

}

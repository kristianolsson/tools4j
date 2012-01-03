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
package org.deephacks.tools4j.config.admin;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.support.event.AbortRuntimeException;

/**
 * <p> 
 * Central interface for provisioning configuration beans to applications. 
 * Configuration is read-only from an application perspective, but can be changed 
 * from this interface making the application reload configuration while it is running.
 * </p>
 * <p>
 * Beans that are fetched will always have their schema initialized, including their 
 * properties and references traversed and fetched eagerly. However, beans that are 
 * provided/given to the admin context does not need to have to have their schema 
 * initalized for operations to work, nor must references be set recusivley (BeanId is 
 * enough to indicate a reference).
 * </p>
 * 
 * <p>
 * Admin Context is specifically not tied to either Java SE, EE, OSGi, Spring, CDI or 
 * any other runtime environment, programming model or framework, even though the goal 
 * is to integrate seamlessly with those. And so the admin context is available for both 
 * server-side application programming, aswell as rich client applications.
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
public abstract class AdminContext {
    private static final String CORE_IMPL = "org.deephacks.tools4j.config.internal.core.admin.AdminCoreContext";

    protected AdminContext() {
        // only core should implement this class
        if (!getClass().getName().equals(CORE_IMPL)) {
            throw new IllegalArgumentException("Only AdminCoreContext is allowed to"
                    + "implement this interface.");
        }
    }

    public static AdminContext get() {
        try {
            return (AdminContext) Class.forName(CORE_IMPL).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a single bean as identified by the id. 
     *
     * @param beanId
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract Bean get(BeanId beanId) throws AbortRuntimeException;

    /**
     * List bean instances of particular type. 
     * 
     * @param type the type of beans to be listed.
     * @return bean of matching type.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract List<Bean> list(String schemaName) throws AbortRuntimeException;

    /**
     * List a sepecific set of bean instances of same specific type. 
     * 
     * @param schemaName the schema name of beans to be listed.
     * @param instanceIds the ids that should be listed.
     * @return bean of matching type.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract List<Bean> list(String schemaName, Collection<String> instanceIds)
            throws AbortRuntimeException;

    /**
     * 
     * @param adminBean
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void create(Bean adminBean) throws AbortRuntimeException;

    /**
     * 
     * @param adminBeans
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void create(Collection<Bean> adminBeans) throws AbortRuntimeException;

    /**
     * Creates or overwrites (set) existing bean instances with provided data.
     * The bean data is considered flat. References will not be traversed 
     * recursively.   
     * <p>
     * Already persisted properties associated with the instance 
     * will be removed if they are missing from the provided bean instances.
     * </p>
     * 
     * @param bean A bean with the values to be written.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void set(Bean bean) throws AbortRuntimeException;

    /**
     * If multiple beans should be written. 
     * <p>
     * Beans should each be provided as a separate element in the list as 
     * initialized bean references not will be traversed and set recursively/eagerly. 
     * </p>
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void set(Collection<Bean> beans) throws AbortRuntimeException;

    /**
     * <p>
     * Merges the provided properties with an already existing instance. 
     * Already persisted properties associated with the instance will be
     * overwritten if they also exist on the provided bean. A property
     * that exist with a null value will be deleted.
     * <p>
     * Values not provided will remain untouched in storage, hence  this method can 
     * be used to set or delete a single property. 
     * </p>
     * <p>
     * Initialized bean references not will be traversed and merged recursively/eagerly. 
     * </p>
     * 
     * @param bean The bean to be merged.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void merge(Bean bean) throws AbortRuntimeException;

    /**
     * This is the collection variant of {@link #merge(Bean)}.  
     * 
     * <p>
     * Beans should each be provided as a separate element in the list as 
     * initialized bean references not will be traversed recursively/eagerly. 
     * </p>
     *  
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void merge(Collection<Bean> beans) throws AbortRuntimeException;

    /**
     * Delete a bean. 
     * <p>
     * A bean that are referenced by other beans cannot be removed as it would
     * violate referential integrity.
     * </p>      
     * <p>
     * Delete operations are not cascading, which means that a bean's references 
     * are not deleted along with the bean itself.
     * </p>.
     * 
     * @param bean to be deleted
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void delete(BeanId bean) throws AbortRuntimeException;

    /**
     * This is the collection variant of {@link #delete(Bean)}.
     * 
     * @param schemaName the name of the schema that covers all instance ids.
     * @param instanceIds instance ids to be deleted.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
     */
    public abstract void delete(String schemaName, Collection<String> instanceIds)
            throws AbortRuntimeException;

    /**
      * Get all schemas available in the system. The keys of the map is the name
      * of the schema. 
      * 
      *@return a map of schemas indexed on schema name.
     * @throws AbortRuntimeException is thrown when the system itself cannot 
     * recover from a certain event and must therefore abort execution, see 
     * {@link org.deephacks.tools4j.config.model.Events}.
      */
    public abstract Map<String, Schema> getSchemas();

}

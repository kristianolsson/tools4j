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
package org.deephacks.tools4j.config.model;

import static com.google.common.base.Objects.equal;
import static org.deephacks.tools4j.config.model.Events.CFG107_MISSING_ID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * <p>
 * Bean represent an instance of a configurable bean. Every bean 
 * that are to be provisioned must first have its corresponding schema 
 * registered in the system.
 * </p>
 * <p>
 * Bean only serve as a simple transfer object and should not be 
 * concerned about schema specific thing.  It should not try to 
 * understand or validate if specific values are correct. 
 * </p>
 * <p>
 * Bean only know about two kinds of values, properties and references. All 
 * values are treated as a list of plain strings and does not care if certain 
 * properties in reality are single valued.
 * </p>
 * @author Kristoffer Sjogren
 */
public class Bean implements Serializable {
    private static final long serialVersionUID = 887497852221101546L;
    private BeanId id;
    private Schema schema;
    private HashMap<String, List<String>> properties = new HashMap<String, List<String>>();
    private HashMap<String, List<BeanId>> references = new HashMap<String, List<BeanId>>();

    private Bean(BeanId id) {
        this.id = Preconditions.checkNotNull(id);
    }

    /**
     * Create a admin bean instance.
     * 
     * @param id unique identification of the bean instance.
     * @return AdminBean
     */
    public static Bean create(BeanId id) {
        Preconditions.checkNotNull(id);
        return new Bean(id);
    }

    /**
     * @return unique identification of the bean instance.
     */
    public BeanId getId() {
        return id;
    }

    /**
     * Schema will only be present if the Bean was fetched or given from
     * a managed administrative context.
     * 
     * @return the schema that belong to the bean instance.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Set the schema that define content structure and constraints of this 
     * the bean instance. 
     * 
     * @param schema
     */
    public void set(Schema schema) {
        this.schema = schema;
    }

    /**
     * Return the list of property names which have values. Properties
     * with default values are not returned.
     *  
     * @return list of property names.
     */
    public List<String> getPropertyNames() {
        ArrayList<String> names = new ArrayList<String>(properties.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Return the list of property names which are references.
     *  
     * @return list of property names.
     */
    public List<String> getReferenceNames() {
        ArrayList<String> names = new ArrayList<String>(references.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Add a list of value to a property on this bean. 
     *  
     * @param propertyName name of the property as defined by the bean's schema. 
     * @param values string representations of the parameterized type of the collection 
     * that conforms to its type as defined by the bean's schema.
     */
    public void addProperty(String propertyName, Collection<String> values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkNotNull(propertyName);
        List<String> list = properties.get(propertyName);
        if (list == null) {
            properties.put(propertyName, new ArrayList<String>(values));
        } else {
            list.addAll(values);
        }
    }

    /**
     * Add a value to a property on this bean. 
     * 
     * @param propertyName name of the property as defined by the bean's schema. 
     * @param value string representations of the property that conforms to 
     * its type as defined by the bean's schema.
     */
    public void addProperty(String propertyName, String value) {
        Preconditions.checkNotNull(propertyName);
        Preconditions.checkNotNull(value);
        List<String> values = properties.get(propertyName);
        if (values == null) {
            values = new ArrayList<String>();
            values.add(value);
            properties.put(propertyName, values);
        } else {
            values.add(value);
        }
    }

    /**
     * Overwrite the current values with the provided value.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @param value string representations of the property that conforms to 
     * its type as defined by the bean's schema.
     */
    public void setProperty(String propertyName, String value) {
        Preconditions.checkNotNull(propertyName);
        if (value == null) {
            properties.put(propertyName, null);
            return;
        }
        List<String> values = new ArrayList<String>();
        values.add(value);
        properties.put(propertyName, values);
    }

    /**
     * Overwrite/replace the current values with the provided values.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @param value string representations of the property that conforms to 
     * its type as defined by the bean's schema.
     */
    public void setProperty(String propertyName, List<String> values) {
        Preconditions.checkNotNull(propertyName);
        if (values == null) {
            properties.put(propertyName, null);
            return;
        }
        properties.put(propertyName, values);
    }

    /**
     * Clear the values of a property or reference, setting it to null. 
     *
     * This operation can be useful when removing properties using "merge" 
     * operation. 
     * 
     * @param name of the property as defined by the bean's schema.
     */
    public void clear(String propertyName) {
        Preconditions.checkNotNull(propertyName);
        if (properties.containsKey(propertyName)) {
            properties.put(propertyName, null);
        } else if (references.containsKey(propertyName)) {
            references.put(propertyName, null);
        }
    }

    /**
     * Remove a property or reference as a property from this bean. 
     * 
     * This operation can be useful when removing properties using "set" 
     * operation. 
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     */
    public void remove(String propertyName) {
        Preconditions.checkNotNull(propertyName);
        if (properties.containsKey(propertyName)) {
            properties.remove(propertyName);
        } else if (references.containsKey(propertyName)) {
            references.remove(propertyName);
        }
    }

    /**
     * Get the values of a property on a bean.
     * 
     * @param name of the property as defined by the bean's schema. 
     * @return string representations of the property that conforms to 
     * its type as defined by the bean's schema.
     */
    public List<String> getValues(String propertyName) {
        Preconditions.checkNotNull(propertyName);
        List<String> values = properties.get(propertyName);
        if (values == null) {
            return null;
        }
        return new ArrayList<String>(values);
    }

    /**
     * A helper method for getting the value of single valued properties. Returns 
     * null if the property does not exist.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @return string representations of the property that conforms to 
     * its type as defined by the bean's schema.
     */
    public String getSingleValue(String propertyName) {
        Preconditions.checkNotNull(propertyName);
        List<String> values = getValues(propertyName);
        if (values == null || values.size() < 1) {
            return null;
        }
        return values.get(0);
    }

    /**
     * Add a list of references to a property on this bean. 
     * 
     * A reference identify other beans based on schema and instance id.
     *  
     * @param propertyName name of the property as defined by the bean's schema.
     * @param ref the reference as defined by the bean's schema.
     */
    public void addReference(String propertyName, List<BeanId> refs) {
        Preconditions.checkNotNull(refs);
        Preconditions.checkNotNull(propertyName);
        List<BeanId> list = references.get(propertyName);
        if (list == null) {
            list = new ArrayList<BeanId>();
            list.addAll(refs);
            references.put(propertyName, list);
        } else {
            list.addAll(refs);
        }
    }

    /**
     * Add a reference to a property on this bean. 
     * 
     * A reference identify other beans based on schema and instance id. 
     * 
     * 
     * @param propertyName name of the property as defined by the bean's schema.  
     * @param ref the reference as defined by the bean's schema.
     */
    public void addReference(String propertyName, BeanId ref) {
        Preconditions.checkNotNull(ref);
        Preconditions.checkNotNull(propertyName);
        List<BeanId> list = references.get(propertyName);
        if (list == null) {
            list = new ArrayList<BeanId>();
            list.add(ref);
            references.put(propertyName, list);
        } else {
            list.add(ref);
        }
    }

    /**
     * Get the references of a property on a bean.
     * 
     * @param propertyName name of the property as defined by the bean's schema. 
     * @return References that identify other beans.
     */
    public List<BeanId> getReference(String propertyName) {
        List<BeanId> values = references.get(propertyName);
        if (values == null) {
            return null;
        }
        return values;
    }

    /**
     * Get all references for all properties of this bean.
     * 
     * @return References that identify other beans.
     */
    public List<BeanId> getReferences() {
        if (references == null) {
            return new ArrayList<BeanId>();
        }
        ArrayList<BeanId> result = new ArrayList<BeanId>();
        for (List<BeanId> b : references.values()) {
            if (b != null) {
                result.addAll(b);
            }

        }
        return result;
    }

    /**
     * Overwrite/replace the current references with the provided reference.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @param value override
     */
    public void setReferences(String propertyName, List<BeanId> values) {
        Preconditions.checkNotNull(propertyName);
        if (values == null || values.size() == 0) {
            references.put(propertyName, null);
            return;
        }
        references.put(propertyName, values);
    }

    /**
     * Overwrite/replace the current references with a provided reference.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @param value override
     */
    public void setReference(String propertyName, BeanId value) {
        Preconditions.checkNotNull(propertyName);
        if (value == null) {
            references.put(propertyName, null);
            return;
        }
        List<BeanId> values = new ArrayList<BeanId>();
        values.add(value);
        references.put(propertyName, values);
    }

    /**
     * A helper method for getting the value of single referenced property. Returns 
     * null if the refrences does not exist.
     * 
     * @param propertyName name of the property as defined by the bean's schema.
     * @return the value.
     */
    public BeanId getFirstReference(String propertyName) {
        List<BeanId> refrences = getReference(propertyName);
        if (refrences == null || refrences.size() < 1) {
            return null;
        }
        return refrences.get(0);
    }

    /**
     * Clears this bean from all properties and references.
     */
    public void clear() {
        properties.clear();
        references.clear();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Bean)) {
            return false;
        }
        Bean other = (Bean) obj;
        return equal(getId(), other.getId());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(Bean.class).add("id", id).add("schema", schema)
                .add("properties", properties).add("references", references).toString();
    }

    /**
     * Identifies bean instances of a particular schema. Instances are unique per id and schema.   
     */
    public static class BeanId implements Serializable {
        private static final long serialVersionUID = -9020756683867340095L;
        private String instanceId;
        private String schemaName;
        private boolean isSingleton;
        private Bean bean;

        private BeanId(String instanceId, String schemaName) {
            this.instanceId = Preconditions.checkNotNull(instanceId);
            this.schemaName = Preconditions.checkNotNull(schemaName);
        }

        private BeanId(String instanceId, String schemaName, boolean isSingleton) {
            this.instanceId = Preconditions.checkNotNull(instanceId);
            this.schemaName = Preconditions.checkNotNull(schemaName);
            this.isSingleton = isSingleton;
        }

        /**
         * Create a bean identification.
         * 
         * @param instanceId of this bean.
         * @param schemaName The bean schema name.  
         * @return AdminBeanId
         */
        public static BeanId create(String instanceId, String schemaName) {
            if (instanceId == null || "".equals(instanceId)) {
                throw CFG107_MISSING_ID();
            }
            return new BeanId(instanceId, schemaName);
        }

        /**
         * This method should NOT be used by users.
         * 
         * @param instanceId this is the singleton id of this bean.
         * @param schemaName schema of bean.
         * @return a singleton id.
         */
        public static BeanId createSingleton(String instanceId, String schemaName) {
            return new BeanId(instanceId, schemaName, true);
        }

        /**
         * @return the instance id of the bean. 
         */
        public String getInstanceId() {
            return instanceId;
        }

        /**
         * @return the schema name of the bean. 
         */
        public String getSchemaName() {
            return schemaName;
        }

        /**
         * Check for singleton.
         * 
         * @return true if singleton.
         */
        public boolean isSingleton() {
            return isSingleton;
        }

        /**
         * Return the bean that is identified by this BeanId. The actual 
         * bean will only be available if the BeanId was initalized by the 
         * from admin context.
         *   
         * @return
         */
        public Bean getBean() {
            return bean;
        }

        /**
         * Do not use. Only used by the admin context. 
         * 
         * @param bean
         */
        public void setBean(Bean bean) {
            this.bean = bean;
        }

        @Override
        public String toString() {
            return getSchemaName() + "@" + getInstanceId();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BeanId)) {
                return false;
            }

            BeanId other = (BeanId) obj;
            return Objects.equal(getInstanceId(), other.getInstanceId())
                    && Objects.equal(getSchemaName(), other.getSchemaName());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getInstanceId(), getSchemaName());
        }
    }

}

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

import java.io.Serializable;
import java.util.Map;

import org.deephacks.tools4j.config.model.Schema;

/**
 * SchemaManager is responsible for management of schemas.
 * 
 * @author Kristoffer Sjogren
 */
public abstract class SchemaManager implements Serializable {
    private static final long serialVersionUID = 4888441728053297694L;

    /**
     * List name of all schemas managed by the manager.
     * 
     * @return list of names.
     */
    public abstract Map<String, Schema> getSchemas();

    /**
     * Return information that describing the schema for a particular type.
     * 
     * @param String
     * @return schema
     */
    public abstract Schema getSchema(String schemaName);

    /**
     * In cluster deployments every server runs same application and 
     * will most probably call this method with exact same name and schema. 
     * Therefore if a schema already exist, it should be overwritten 
     * by the provided schema.
     * <p>
     * This method also binds together interaction between different
     * manager implementations through specific properties.
     * </p>
     * 
     * @param name Name of the model.
     * @param info A bean info hierarchy representing its model, and
     *            also model specific properties.
     */
    public abstract void regsiterSchema(Schema... schema);

    /**
     * Removes the schema and will not longer be managed by this
     * configuration manager.
     * <p>
     * This operation DOES NOT remove beans instances that are 
     * associated with the schema that is to be removed.
     * </p>
     * 
     * @param name Class that represent the schema.
     */
    public abstract void removeSchema(String schemaName);

}

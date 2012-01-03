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

import java.util.Collection;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.support.event.AbortRuntimeException;

/**
 * <p>
 * When beans are created or updated, it is the responsibility of the
 * validation manager of to maintain data integrity by enforcing validation 
 * constraints and reject operations that violate these rules.
 * </p>
 * A validation manager should not try to do schema validation, ie check 
 * data types or referential integrity etc.  
 * <p>
 * A validation manager is responsible for inspecting schemas and extract 
 * validation rules that are understood by this manager. This process occurs
 * when the configurable classes are registered in the system.
 * </p>
 * <p>
 * Validation Managers are free to implement any validation mechanism, but
 * are encouraged to support JSR 303, Bean Validation.
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
public abstract class ValidationManager {
    /**
     * Called when a new configurable class is registered in the system. 
     * 
     * @param schemaName this is the schema name that identify the class.
     * @param clazz the configurable class
     * @throws AbortRuntimeException
     */
    public abstract void register(String schemaName, Class<?> clazz) throws AbortRuntimeException;

    /**
     * Validate a collection of bean instances. This method is called
     * when beans are provisioned from an administrative context.
     * <p>
     * Beans can correlate their respective validation constraints using
     * the schema name.
     * </p>
     * 
     * @param beans to be validated.
     * @throws AbortRuntimeException 
     */
    public abstract void validate(Collection<Bean> beans) throws AbortRuntimeException;
}

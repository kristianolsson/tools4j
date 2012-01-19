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
package org.deephacks.tools4j.config;

import java.util.List;

/**
 * <p> 
 * Central interface for providing typesafe configuration reliably to applications, 
 * that conforms to the constraints defined by the application. Configuration is read-only 
 * from an application perspective, but may be reloaded/changed from administrative 
 * context when the application is running.
 * </p>
 * <p>
 * Application should not cache configuration instance that are fetched from the runtime
 * context.
 * </p>
 * <p>
 * TODO: Application can register event listeners that triggers when configuration changed. 
 * </p>
 * Application registered configured classes with this runtime context in order to make them
 * visible and available for provisioning in an administrative context.
 * <p> 
 * If the same class is registered multiple times, the former class is 
 * simply replaced. Keep this in mind when configuration schema is changed/upgraded. 
 * It is possible to register a schema that will make the administrative context to 
 * malfunction if the new class is not compatible with earlier versions of data of the 
 * same class.
 * <p>
 * Runtime Context is specifically not tied to either Java SE, EE, OSGi, Spring, CDI or 
 * any other runtime environment, programming model or framework, even though the goal 
 * is to integrate seamlessly with those. And so the runtime context is available for both 
 * server-side application programming, aswell as rich client applications.
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
public abstract class RuntimeContext {
    private static final String CORE_IMPL = "org.deephacks.tools4j.config.internal.core.runtime.RuntimeCoreContext";

    protected RuntimeContext() {
        // only core should implement this class
        if (!getClass().getName().equals(CORE_IMPL)) {
            throw new IllegalArgumentException("Only RuntimeCoreContext is allowed to"
                    + "implement this interface.");
        }
    }

    public static RuntimeContext get() {
        try {
            return (RuntimeContext) Class.forName(CORE_IMPL).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If @Config changes name between upgrades, the new schema will be registered as 
     * a new while the old one will persist. 
     * 
     * @param configurable
     */
    public abstract void register(Class<?>... configurable);

    public abstract void unregister(Class<?>... configurable);

    /**
     * Read a singleton instance. This requires the configurable to have a 
     * <b>static</b> <b>final</b> {@link Id} with a default value assigned.  
     * 
     * @param clazz A configurable class
     * @return
     */
    public abstract <T> T singleton(Class<T> configurable);

    public abstract <T> List<T> all(Class<T> configurable);

    public abstract <T> T get(String id, Class<T> configurable);

}

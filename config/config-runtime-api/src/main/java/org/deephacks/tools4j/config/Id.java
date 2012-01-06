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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Id uniquely identify a configuration instance similar to how primary key are used within
 * databases.
 * <p>
 * Ids are used by administrators to link configurations together. It may therefore
 * be beneficial to choose an id that assist intuition for making these realtionships.  
 * But be careful choosing meaning for ids, they define uniqueness and are not allowed change after 
 * they have been created.
 * </p>
 * <p>
 * Every configurable class must annotate one (and only one) field with this annotation so 
 * that specific instances can be addressed.  
 * </p>
 * <p>
 * Configurable classes can be singletons. This means only one instance with an id that cannot change and  
 * instantiation/removal are restricted. Properties, though, are allowed to change.
 * The instance is automatically created when the class is registered. If the id already exist, 
 * creation will be silently ignored (i.e. no attempt merge or update). Singeltons are read preferably using
 * {@link RuntimeContext#singleton(Class)}
 * </p>
 * <p>
 * <ul>
 * <li>Id field must only have a single value of {@link java.lang.String} type.</li>
 * <li>Id field can be <b>final</b>, in which a default value is required. This is a singleton.</li>
 * <li>Id field are not allowed to be non-<b>final</b> <b>static</b>.</li>
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface Id {
    /**
     * A name can have a real world meaning (like social security numbers, account numbers etc) 
     * or be entirely artificial. The name should reflect this intent.
     * <p>
     * Name must be unique for all configuration classes of same type.
     * </p>
     */
    String name();

    /**
     * Description can be used to inform if identification is intended to use attributes 
     * that exist in the real world ) or relate to already eastablished system architecture 
     * aspects/concepts.
     * 
     * @return
     */
    String desc();
}

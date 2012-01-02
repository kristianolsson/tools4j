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
 * A property indentify a single configurable detail that is configurable within a 
 * larger, cohesive concept. It should be possible to change properties atomically in 
 * isolation, that is, changing a property should not coincidentally force other properties 
 * to change in order to guarantee predicatble behaviour.  
 * 
 * <p>
 * Changing a property should never cause system failure or malfunctioning. Make sure 
 * properties have proper validation rules so that administrators cannot accidentally 
 * misconfigure the system.
 * </p>
 * <p>
 * Properties are used to mark a fields of configurable classes as configurable.
 * </p>
 * 
 * <p>
 * Property fields can be Collection 
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface Property {
    /**
     * Names are informative and comprehensible by an administrative user that
     * clearly reveales the intent of the property.
     * <p>
     * Names will be displayed to administrative users. 
     * </p> 
     * @return
     */
    String name();

    /**
     * <p>
     * An informative description of the property that tells why it exists, 
     * what it does, how it is used and how changes affect the behaviour 
     * of the system.
     * <p>
     * Properties within the same class may affect each other, and if so,
     * do describe these inter-dependencies. 
     * </p>
     * <p>
     * Description will be displayed to administrative users. 
     * </p>
     * @return A description.
     */
    String desc();
}

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
package org.deephacks.tools4j.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CliOption is responsible for adding an option to a specific command.
 * 
 * At command line, options begin with a hyphen delimiter (‘-’).
 * 
 * Multiple options may follow a hyphen delimiter in a single token if the options do 
 * not take arguments. Thus, ‘-abc’ is equivalent to ‘-a -b -c’.
 * 
 * Options can have arguments. Argument-less options must only be annotated on variables 
 * of type Boolean.  
 * 
 * @author Kristoffer Sjogren
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface CliOption {
    /**
     * The short name of the parameter is used if name is provided through a
     * single dash "-" So a shortName of "p" is provided as "-p"
     * 
     * @return The shorter name of the parameter.
     */
    String shortName();

    /**
     * The full name of the parameter. The full name is activated by a double
     * dash "--". So a name of "provided" is provided as "--provided"
     * 
     * @return The long name of the parameter.
     */
    String name() default "";

    /**
     * A description of this option that will be used for displaying a help screen or 
     * through generated documentation.
     * 
     * @return String.
     */
    String desc();

}

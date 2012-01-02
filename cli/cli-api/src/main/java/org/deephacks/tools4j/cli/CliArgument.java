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
 * CliArgument is responsible for indicating an argument on a CliCommand.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface CliArgument {
    /**
     * Only for display purposes at help screen.
     * @return name of the argument.
     */
    String name();

    /**
     * An informative description of the argument that will be used to display help screen.
     * @return description of the argument.
     */
    String desc();

    /**
     * Position starts at zero, so first argument will be identified at position 0.
     * 
     * @return position of the argument.
     */
    int position();

}

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
package org.deephacks.tools4j.support.web.jpa;

import java.text.MessageFormat;

import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.deephacks.tools4j.support.event.Event;
import org.deephacks.tools4j.support.event.EventDoc;

public class JpaEvents {
    /**
     * Unique identifier of this module: {@value} 
     */
    public static final String MODULE_NAME = "tools4j.config.jpa";
    /**
     * {@value} - Success. 
     */
    public static final int JPA001 = 1;
    static final String JPA001_MSG = "Success.";

    @EventDoc(module = MODULE_NAME, code = JPA001, desc = JPA001_MSG)
    static Event JPA001_SUCCESS() {
        return new Event(MODULE_NAME, JPA001, JPA001_MSG);
    }

    /**
     * {@value} - JPA property filepath system variable not set. 
     */
    public static final int JPA201 = 201;
    private static final String JPA201_MSG = "JPA property filepath system variable [{0}] not set.";

    @EventDoc(module = MODULE_NAME, code = JPA201, desc = JPA201_MSG)
    public static AbortRuntimeException JPA201_PROP_FILEPATH_MISSING(String prop) {
        Event event = new Event(MODULE_NAME, JPA201, MessageFormat.format(JPA201_MSG, prop));
        throw new AbortRuntimeException(event);
    }

    /**
     * {@value} - An Entity Manager was not found with the current thread local. 
     */
    public static final int JPA202 = 202;
    private static final String JPA202_MSG = "An Entity Manager was not found with the current thread local.";

    @EventDoc(module = MODULE_NAME, code = JPA202, desc = JPA201_MSG)
    public static AbortRuntimeException JPA202_MISSING_THREAD_EM() {
        Event event = new Event(MODULE_NAME, JPA202, JPA202_MSG);
        throw new AbortRuntimeException(event);
    }
}

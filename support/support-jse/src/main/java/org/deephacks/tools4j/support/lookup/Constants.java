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
package org.deephacks.tools4j.support.lookup;

/**
 * Constants is responsible for managing all constants within this module.
 */
public class Constants {
    private static final String LOG_MSG_LOOKUP_OVERRIDE_MSG = "System property successfully overrides default lookup to {}";
    private static final String LOG_MSG_LOOKUP_SYS_ERROR_MSG = "Could not find class indicated by system property %s, using default lookup instead.";

    public static final String LOG_MSG_LOOKUP_SYS_ERROR(String className) {
        return String.format(LOG_MSG_LOOKUP_SYS_ERROR_MSG, className);
    }

    public static final String LOG_MSG_LOOKUP_OVERRIDE(String className) {
        return String.format(LOG_MSG_LOOKUP_OVERRIDE_MSG, className);
    }
}

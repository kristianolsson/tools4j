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
package org.deephacks.tools4j.log;

import org.slf4j.Logger;

/**
 * Log is responsible for handling log conventions and reducing log clutter. 
 */
public class Log {
    public static void logDebugException(Logger logger, String msg, Throwable e) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg, e);
            return;
        }
        logger.warn(msg);
    }
}

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
package org.deephacks.tools4j.support.event;

import com.google.common.base.Preconditions;

/**
 * RecoverableException is thrown when it is resonable to believe that the caller/system can recover from the 
 * error itself. For example, retrying a timeout, fallback to a deafult behaviour, retry from interuption or 
 * ignoring the failure and continue execution. Or, last resort, propagate this exception as a 
 * AbortRuntimeException.
 * 
 * This exception is checked and should be extended into a more informative exception that clearly describe 
 * the exception situation.
 * 
 * @author Kristoffer Sjogren
 */
public abstract class RecoverableException extends Exception {
    private static final long serialVersionUID = -7994691832123397253L;
    private Event event;

    /**
     *  
     * @param event this exception was identified to be caused by this event.
     */
    public RecoverableException(Event event) {
        this.event = Preconditions.checkNotNull(event);
    }

    /**
     * 
     * @param event this exception was identified to be caused by this event.
     * @param e The exception that caused the exception.
     */
    public RecoverableException(Event event, Exception e) {
        super(e);
        this.event = Preconditions.checkNotNull(event);
    }

    /**
     * Return the code that clearly identifies the event that occured.
     * 
     * @return a status code.
     */
    public Event getStatusCode() {
        return event;
    }

    /**
     * A human readable and informative error message.
     */
    @Override
    public String getMessage() {
        return event.toString();
    }

}

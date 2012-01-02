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

import org.deephacks.tools4j.support.event.AbortRuntimeException;

/**
 * All commands that are created should implement this interface and annotated with 
 * {@link CliExtension}.
 * 
 * Commands are located at runtime using {@link java.lang.ServiceLoader} mechanism.  
 */
public interface CliCommand {
    /**
     * Will be executed when this command is called, the context is initialized, the 
     * input data have been validated and it is time to start executing the command. 
     *
     * @param ctx Contains contextual information that may be needed for executing the 
     * command. 
     * 
     * @throws AbortRuntimeException throw this exception with the causing event and
     * the framework will take care of the rest.
     */
    public void execute(CliExecutionContext ctx) throws AbortRuntimeException;

}

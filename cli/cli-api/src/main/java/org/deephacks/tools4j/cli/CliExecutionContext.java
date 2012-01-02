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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.deephacks.tools4j.support.reflections.BeanInstance;

import com.google.common.base.Preconditions;

/**
 * CliExecutionContext is responsible for encapsulating contextual information that is related to 
 * execution of the command.
 */
public final class CliExecutionContext {
    private CliConsole logger;
    private GNUishParser parser;
    private CliExtensionValidator validator;
    private BeanInstance<CliCommand> cliCommand;
    private Collection<BeanInstance<CliCommand>> availableCliCommands = new ArrayList<BeanInstance<CliCommand>>();
    private File currentDirectory;
    private static ThreadLocal<CliExecutionContext> THREAD_CONTEXT = new ThreadLocal<CliExecutionContext>();

    CliExecutionContext(GNUishParser parser, BeanInstance<CliCommand> cliCommand) {
        this.parser = Preconditions.checkNotNull(parser);
        this.cliCommand = Preconditions.checkNotNull(cliCommand);
        try {
            currentDirectory = new File(".").getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        THREAD_CONTEXT.set(this);
    }

    public static CliExecutionContext get() {
        return THREAD_CONTEXT.get();
    }

    /**
     * Execute the command. 
     * 
     * @throws AbortRuntimeException throw this exception with the causing event and
     * the framework will take care of the rest.
     */
    void execute() throws AbortRuntimeException {
        validator = new CliExtensionValidator(cliCommand);
        validator.validateCliOptionMetadata();
        validator.validateCliArgumentMetadata();
        new CliCommandInjector(parser, cliCommand).inject();
        validator.validateCommandInput();
        cliCommand.get().execute(this);
    }

    /**
     * Get the terminal directory from where this command was executed.
     *  
     * @return A directory.
     */
    public File getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * The arguments which are not related to an option.
     * 
     * @return a collection of arguments.
     */
    public Collection<String> getTrailingArguments() {
        return parser.getArgs();
    }

    /**
     * Get the console for displaying output in the terminal.
     * @return
     */
    public CliConsole getConsole() {
        if (logger != null) {
            return logger;
        }

        if (parser.verbose()) {
            logger = CliConsole.createVerbose();
            return logger;
        }
        logger = CliConsole.createStandard();
        return logger;
    }

    BeanInstance<CliCommand> getCommand() {
        return cliCommand;
    }

    Collection<BeanInstance<CliCommand>> getAvailableCommands() {
        return availableCliCommands;
    }

}

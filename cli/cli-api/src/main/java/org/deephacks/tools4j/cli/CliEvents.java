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
import java.text.MessageFormat;

import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.deephacks.tools4j.support.event.Event;
import org.deephacks.tools4j.support.event.EventDoc;
import org.deephacks.tools4j.support.reflections.BeanAnnotatedField;

/**
 * Events is responsible for consolidating all known events that may occur when iteracting 
 * with this module. 
 * 
 * @author Kristoffer Sjogren.
 */
public class CliEvents {
    /**
     * Unique identifier of this module: {@value} 
     */
    public static final String MODULE_NAME = "tools4j.cli";
    /**
     * {@value} - Success. 
     */
    public static final int CLI001 = 1;
    static final String CLI001_MSG = "Success.";

    @EventDoc(module = MODULE_NAME, code = CLI001, desc = CLI001_MSG)
    static Event CLI001_SUCCESS() {
        return new Event(MODULE_NAME, CLI001, CLI001_MSG);
    }

    /**
     * {@value} - Display command help screen.
     */
    public static final int CLI002 = 2;
    static final String CLI002_MSG = "Help screen for command {0} was displayed.";

    @EventDoc(module = MODULE_NAME, code = CLI002, desc = "Displays command help screen.")
    static Event CLI002_HELP_COMMAND(String command) {
        return new Event(MODULE_NAME, CLI002, MessageFormat.format(CLI002_MSG, command));
    }

    /**
     * {@value} - Display available command help screen.
     */
    public static final int CLI003 = 3;
    static final String CLI003_MSG = "Help screen for available commands was displayed";

    @EventDoc(module = MODULE_NAME, code = CLI003, desc = "Displays available command help screen.")
    static Event CLI003_HELP_COMMANDS() {
        return new Event(MODULE_NAME, CLI003, CLI003_MSG);
    }

    /**
     * {@value} - Unexpected exception.
     */
    public static final int CLI004 = 4;
    static final String CLI004_MSG = "Unexpected exception: {0} {1}";

    @EventDoc(module = MODULE_NAME, code = CLI004, desc = "Unexpected exception.")
    static Event CLI004_UNEXPECTED_EXCEPTION(Throwable e) {
        String amsg = e.getMessage() == null ? "null" : e.getMessage();

        String msg = MessageFormat.format(CLI004_MSG, e.getClass().getName(), amsg);
        Event code = new Event(MODULE_NAME, CLI004, msg);
        return code;
    }

    /**
     * {@value} - Invalid option argument value.
     */
    public static final int CLI101 = 101;
    static final String CLI101_MSG = "Option must have value of Type {0}: {1}";

    @EventDoc(module = MODULE_NAME, code = CLI101, desc = "Invalid option argument value.")
    static AbortRuntimeException CLI101_OPTION_INVALID_INPUT(String inputFormat, String optionName)
            throws AbortRuntimeException {
        String msg = MessageFormat.format(CLI101_MSG, inputFormat, optionName);
        Event code = new Event(MODULE_NAME, CLI101, msg);
        return new AbortRuntimeException(code);
    }

    static AbortRuntimeException CLI101_OPTION_INVALID_INPUT(BeanAnnotatedField<CliOption> field)
            throws AbortRuntimeException {
        String optionName = field.getAnnotation().shortName();
        String inputFormat = CliExtensionValidator.getInputFormatString(field.getType());
        return CLI101_OPTION_INVALID_INPUT(inputFormat, optionName);
    }

    /**
     * {@value} - Invalid argument input value. 
     */
    public static final int CLI102 = 102;
    static final String CLI102_MSG = "Argument must have value of Type {0}: {1}";

    @EventDoc(module = MODULE_NAME, code = CLI102, desc = "Invalid argument input value.")
    static AbortRuntimeException CLI102_ARGUMENT_INVALID_INPUT(String inputFormat,
            String argumentName) {
        String msg = MessageFormat.format(CLI102_MSG, inputFormat, argumentName);
        Event code = new Event(MODULE_NAME, CLI102, msg);
        return new AbortRuntimeException(code);
    }

    static AbortRuntimeException CLI102_ARGUMENT_INVALID_INPUT(BeanAnnotatedField<CliArgument> field)
            throws AbortRuntimeException {
        String optionName = field.getAnnotation().name();
        String inputFormat = CliExtensionValidator.getInputFormatString(field.getType());
        return CLI102_ARGUMENT_INVALID_INPUT(inputFormat, optionName);
    }

    /**
     * {@value} - Missing required argument input.
     */
    public static final int CLI103 = 103;
    static final String CLI103_MSG = "Missing required argument: {0}";

    @EventDoc(module = MODULE_NAME, code = CLI103, desc = "Missing required argument input.")
    static AbortRuntimeException CLI103_ARGUMENT_INPUT_MISSING(String argumentName) {
        String msg = MessageFormat.format(CLI103_MSG, argumentName);
        Event code = new Event(MODULE_NAME, CLI103, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Input violated a constraint.
     */
    public static final int CLI104 = 104;
    static final String CLI104_MSG = "Input constraint violated: {0} {1}";

    @EventDoc(module = MODULE_NAME, code = CLI104, desc = "Input violated a input constraint.")
    static AbortRuntimeException CLI104_INPUT_CONSTRAINT_VIOLATION(String optionName,
            String violationText) {
        String msg = MessageFormat.format(CLI104_MSG, optionName, violationText);
        Event code = new Event(MODULE_NAME, CLI104, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Argument does not have a position.
     */
    public static final int CLI105 = 105;
    static final String CLI105_MSG = "@CliExtension {0} has a @CliArgument with position {1} missing.";

    @EventDoc(module = MODULE_NAME, code = CLI105, desc = "Argument does not have a position.")
    static AbortRuntimeException CLI105_ARG_POSITION_MISSING(String extensionName, int pos) {
        String msg = MessageFormat.format(CLI105_MSG, extensionName, pos);
        Event code = new Event(MODULE_NAME, CLI105, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Non unique argument or option identification.
     */
    public static final int CLI106 = 106;
    static final String CLI106_MSG = "@CliExtension {0} has a @CliArgument or @CliOption with duplicate name or shortnames.";

    @EventDoc(module = MODULE_NAME, code = CLI106,
            desc = "Non unique argument or option identification.")
    static AbortRuntimeException CLI106_DUPLICATE_ARG_OPT_ID(String extensionName) {
        String msg = MessageFormat.format(CLI106_MSG, extensionName);
        Event code = new Event(MODULE_NAME, CLI106, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Argument or option is using reserved names. 
     */

    public static final int CLI107 = 107;
    static final String CLI107_MSG = "@CliExtension {0} are using reserved short names (v or d) or names (verbose, debug).";

    @EventDoc(module = MODULE_NAME, code = CLI107,
            desc = "Argument or option is using reserved names.")
    static AbortRuntimeException CLI107_RESERVED_ARG_OPT_NAME(String extensionName) {
        String msg = MessageFormat.format(CLI107_MSG, extensionName);
        Event code = new Event(MODULE_NAME, CLI107, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Duplicate commands found. 
     */
    public static final int CLI108 = 108;
    static final String CLI108_MSG = "There are two @CliExtension with same name:  {0}";

    @EventDoc(module = MODULE_NAME, code = CLI108, desc = "Duplicate commands found.")
    static AbortRuntimeException CLI108_DUPLICATE_COMMANDS(String extensionName) {
        String msg = MessageFormat.format(CLI108_MSG, extensionName);
        Event code = new Event(MODULE_NAME, CLI108, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Command does not exist.
     */
    public static final int CLI109 = 109;
    static final String CLI109_MSG = "Command does not exist:  {0}";

    @EventDoc(module = MODULE_NAME, code = CLI109, desc = "Command does not exist.")
    static AbortRuntimeException CLI109_COMMAND_DOES_NOT_EXIST(String extensionName) {
        String msg = MessageFormat.format(CLI109_MSG, extensionName);
        Event code = new Event(MODULE_NAME, CLI109, msg);
        return new AbortRuntimeException(code);
    }

    /**
     * {@value} - Configuration file does not exist.
     */
    public static final int CLI110 = 110;
    static final String CLI110_MSG = "Configuration file does not exist:  {0}";

    @EventDoc(module = MODULE_NAME, code = CLI110, desc = "Configuration file does not exist.")
    static AbortRuntimeException CLI110_CONF_DOES_NOT_EXIST(File file) {
        String msg = MessageFormat.format(CLI110_MSG, file.getAbsolutePath());
        Event code = new Event(MODULE_NAME, CLI110, msg);
        return new AbortRuntimeException(code);
    }
}

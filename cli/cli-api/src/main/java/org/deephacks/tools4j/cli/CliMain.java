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

import static org.deephacks.tools4j.cli.CliEvents.CLI001_SUCCESS;
import static org.deephacks.tools4j.cli.CliEvents.CLI002_HELP_COMMAND;
import static org.deephacks.tools4j.cli.CliEvents.CLI003_HELP_COMMANDS;
import static org.deephacks.tools4j.cli.CliEvents.CLI108_DUPLICATE_COMMANDS;
import static org.deephacks.tools4j.cli.CliEvents.CLI109_COMMAND_DOES_NOT_EXIST;
import static org.deephacks.tools4j.cli.CliEvents.CLI110_CONF_DOES_NOT_EXIST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.log.LogConfiguration;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.deephacks.tools4j.support.event.Event;
import org.deephacks.tools4j.support.lookup.Lookup;
import org.deephacks.tools4j.support.reflections.BeanAnnotatedField;
import org.deephacks.tools4j.support.reflections.BeanInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Main class that is called by the bootstrap when classpath have been initalized and it is time to start 
 * executing.  
 * 
 * @author Kristoffer Sjogren 
 */
class CliMain {
    static final String CONF_DIR = "conf";
    static final String CONF_FILE = "conf.properties";
    private Collection<BeanInstance<CliCommand>> availableCliCommands = new ArrayList<BeanInstance<CliCommand>>();
    private BeanInstance<CliCommand> cliCommand;
    private String terminalArgs[];
    private Logger logger = null;
    private AbortRuntimeException constructorEx;

    public CliMain(String terminalArgs[]) {
        if (terminalArgs == null) {
            terminalArgs = new String[0];
        }
        try {
            LogConfiguration.init(getConfDir());
        } catch (AbortRuntimeException e) {
            // cannot throw yet, constructor cannot return the event.
            constructorEx = e;
        }

        this.terminalArgs = terminalArgs;

        if (GNUishParser.parseReservedOnly(terminalArgs).debug()) {
            LogConfiguration.setDebug();
        }

        this.logger = LoggerFactory.getLogger(CliMain.class);
        availableCliCommands = getLookups();

    }

    public Event run() {
        if (terminalArgs == null || terminalArgs.length == 0) {
            // no command provided, show what commands are available.
            printAvailableCommandsHelp();
            return CLI003_HELP_COMMANDS();
        }
        String command = terminalArgs[0];
        // strip command from args after parsing
        String[] remainingArgs = stripFirst(terminalArgs);
        if (command == null || "".equals(command)) {
            printAvailableCommandsHelp();
            return CLI003_HELP_COMMANDS();
        }
        try {
            if (constructorEx != null) {
                throw constructorEx;
            }
            validateUniqueness();
            cliCommand = getCommand(command);
            List<String> nonArgumentedOptions = getNonArgumentedOptions(cliCommand);
            GNUishParser p = GNUishParser.parse(remainingArgs, nonArgumentedOptions);
            if (p.help() && cliCommand != null) {
                printCommandHelp();
                return CLI002_HELP_COMMAND(command);
            }
            CliExecutionContext ctx = new CliExecutionContext(p, cliCommand);
            ctx.execute();
        } catch (AbortRuntimeException e) {
            System.out.println(e.getMessage());
            logger.debug(e.getMessage(), e);
            return e.getEvent();
        } catch (Throwable e) {
            Event code = CliEvents.CLI004_UNEXPECTED_EXCEPTION(e);
            System.out.println(code.getMessage());
            logger.debug(e.getMessage(), e);
            return code;
        }
        return CLI001_SUCCESS();
    }

    BeanInstance<CliCommand> getCommand(String command) {
        for (BeanInstance<CliCommand> aCommand : availableCliCommands) {
            CliExtension extension = aCommand.getClassAnnotation(CliExtension.class);
            if (command.equals(extension.keyword())) {
                cliCommand = aCommand;
            }
        }
        if (cliCommand == null) {
            throw CLI109_COMMAND_DOES_NOT_EXIST(command);
        }
        return cliCommand;
    }

    private Collection<BeanInstance<CliCommand>> getLookups() {
        Collection<CliCommand> lookups = Lookup.get().lookupAll(CliCommand.class);
        logger.debug("CliCommands are {}", lookups);
        return BeanInstance.of(lookups);

    }

    private void validateUniqueness() {
        Map<String, BeanInstance<CliCommand>> availableCommands = Maps.newHashMap();
        for (BeanInstance<CliCommand> command : availableCliCommands) {
            CliExtension ext = command.getClassAnnotation(CliExtension.class);
            if (availableCommands.get(ext.keyword()) != null) {
                throw CLI108_DUPLICATE_COMMANDS(ext.keyword());
            }
            availableCommands.put(ext.keyword(), command);
        }
    }

    /**
     * We need to know which options are argumented and which ones are not.
     * The only field considered non-argumented is java.lang.Boolean.
     * 
     * --verbose, -v, --debug and -d, --help, -h are reserved non-argumented options.
     * 
     * @return
     */
    public List<String> getNonArgumentedOptions(BeanInstance<CliCommand> target) {
        List<String> nonArgumentedOptions = new ArrayList<String>();
        for (BeanAnnotatedField<CliOption> opt : target.findFieldsAnnotatedWith(CliOption.class)
                .values()) {
            if ("java.lang.Boolean".equals(opt.getTypeString())) {
                nonArgumentedOptions.add(opt.getAnnotation().shortName());
                nonArgumentedOptions.add(opt.getAnnotation().name());
            }
        }
        nonArgumentedOptions.addAll(GNUishParser.getReservedNonArgumentOptions());
        return nonArgumentedOptions;
    }

    public final static File getConfDir() {
        File file = new File(Bootstrap.getHomeDir(), CONF_DIR);
        try {
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
            return file.getCanonicalFile();
        } catch (FileNotFoundException e) {
            throw CLI110_CONF_DOES_NOT_EXIST(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] stripFirst(String[] args) {
        String[] argz = new String[args.length - 1];
        System.arraycopy(args, 1, argz, 0, args.length - 1);
        return argz;
    }

    private void printAvailableCommandsHelp() {
        StringBuffer sb = new StringBuffer();
        sb.append("Available commands are: ");
        for (BeanInstance<CliCommand> cliCommand : availableCliCommands) {
            CliExtension ext = cliCommand.getClassAnnotation(CliExtension.class);
            sb.append("  " + ext.keyword() + " - " + ext.usage());
        }
        System.out.println(sb.toString());
    }

    private void printCommandHelp() {
        CliExtension cliExtension = cliCommand.getClassAnnotation(CliExtension.class);
        StringBuffer sb = new StringBuffer();
        Map<String, BeanAnnotatedField<CliArgument>> arguments = cliCommand
                .findFieldsAnnotatedWith(CliArgument.class);

        Map<Integer, BeanAnnotatedField<CliArgument>> sortedArguments = new HashMap<Integer, BeanAnnotatedField<CliArgument>>();
        for (BeanAnnotatedField<CliArgument> argument : arguments.values()) {
            sortedArguments.put(argument.getAnnotation().position(), argument);
        }

        StringBuffer args = new StringBuffer();
        for (int i = 0; i < sortedArguments.size(); i++) {
            BeanAnnotatedField<CliArgument> a = sortedArguments.get(i);

            args.append(a.getAnnotation().name()).append(" ");
        }

        sb.append("usage: ").append(cliExtension.keyword())
                .append(" [OPTIONS...] " + args + "\n\n");
        sb.append("  ").append(cliExtension.usage()).append("\n\n");

        sb.append("ARGUMENTS\n");
        sb.append("\n");
        for (int i = 0; i < sortedArguments.size(); i++) {
            CliArgument arg = sortedArguments.get(i).getAnnotation();
            sb.append(" ").append(arg.name());
            sb.append(" : ").append(arg.desc());
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("OPTIONS\n");
        Map<String, BeanAnnotatedField<CliOption>> options = cliCommand
                .findFieldsAnnotatedWith(CliOption.class);
        for (BeanAnnotatedField<CliOption> option : options.values()) {
            CliOption opt = option.getAnnotation();
            sb.append(" ");
            sb.append("-").append(opt.shortName()).append(",");
            sb.append("--").append(opt.name()).append(" ");
            sb.append("<").append(CliExtensionValidator.getInputFormatString(option.getType()))
                    .append(">");
            sb.append(" : ").append(opt.desc());
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * GNUishParser is responsible for parsing the command line arguments more or less 
 * according to the GNU Argument Syntax: 
 * 
 * http://www.gnu.org/s/hello/manual/libc/Argument-Syntax.html.
 */
class GNUishParser {
    private Map<String, String> shortOpts = new HashMap<String, String>();
    private Map<String, String> longOpts = new HashMap<String, String>();
    private List<String> arguments = new ArrayList<String>();
    private List<String> nonArgumentedOptions = new ArrayList<String>();
    private static Logger LOGGER = LoggerFactory.getLogger(GNUishParser.class);

    private static String VERBOSE_SHORT_OPT = "v";
    private static String VERBOSE_LONG_OPT = "verbose";
    private static String DEBUG_SHORT_OPT = "d";
    private static String DEBUG_LONG_OPT = "debug";
    private static String HELP_SHORT_OPT = "h";
    private static String HELP_LONG_OPT = "help";

    private GNUishParser(List<String> nonArgumentedOptions) {
        this.nonArgumentedOptions = nonArgumentedOptions;
    }

    static GNUishParser parse(String[] terminalArgs, List<String> nonArgumentedOptions) {
        GNUishParser p = new GNUishParser(nonArgumentedOptions);
        if (terminalArgs == null || terminalArgs.length == 0) {
            return p;
        }
        // we strip opts after parsing
        terminalArgs = p.parseOpts(terminalArgs);

        // arguments is what's left
        p.arguments = Arrays.asList(terminalArgs);
        LOGGER.debug("Arguments are {}", p.arguments);
        return p;
    }

    static GNUishParser parseReservedOnly(String[] terminalArgs) {
        GNUishParser p = new GNUishParser(getReservedNonArgumentOptions());
        if (terminalArgs == null || terminalArgs.length == 0) {
            return p;
        }
        // we strip opts after parsing
        terminalArgs = p.parseOpts(terminalArgs);

        // arguments is what's left
        p.arguments = Arrays.asList(terminalArgs);
        LOGGER.debug("Arguments are {}", p.arguments);
        return p;
    }

    /**
     * Parse the options for the command.
     * @param args includes the options and arguments, command word have been stripped.
     * @return the remaining terminal args, if any
     */
    private String[] parseOpts(String[] args) {

        if (args == null || args.length == 0) {
            return new String[0];
        }
        List<String> remainingArgs = new ArrayList<String>();
        List<String> argsList = Arrays.asList(args);
        ListIterator<String> argsIt = argsList.listIterator();

        while (argsIt.hasNext()) {
            String word = argsIt.next();
            if (word.startsWith("--")) {
                // long option --foo
                String option = stripLeadingHyphens(word);
                // only slurp argument if option is argumented
                if (!nonArgumentedOptions.contains(option)) {
                    String arg = parseOptionArg(option, argsIt);
                    longOpts.put(option, arg);
                } else {
                    // otherwise consider the option as "enabled"
                    longOpts.put(option, "true");
                }
            } else if (word.startsWith("-")) {
                String options = stripLeadingHyphens(word);

                // single short option -f
                if (options.length() == 1) {
                    // only slurp argument if option is argumented
                    if (!nonArgumentedOptions.contains(options)) {
                        String arg = parseOptionArg(options, argsIt);
                        shortOpts.put(options, arg);
                    } else {
                        // otherwise consider the option as "enabled"
                        shortOpts.put(options, "true");
                    }
                    continue;
                }
                // multiple short options -fxy, 
                // treat as non-argumented java.lang.Boolean variables, no slurp 
                for (int i = 0; i < options.length(); i++) {
                    String option = Character.toString(options.charAt(i));
                    shortOpts.put(option, "true");
                }
            } else {
                remainingArgs.add(word);
            }
        }
        LOGGER.debug("{}", this);
        return remainingArgs.toArray(new String[0]);
    }

    private String parseOptionArg(String option, ListIterator<String> argsIt) {
        if (!argsIt.hasNext()) {
            // no argument. assume boolean opt
            return "true";
        }

        String arg = argsIt.next();
        // the token following the option is a new option (not an argument)
        if (arg.startsWith("-")) {
            // arg was the next opt, take one step back 
            argsIt.previous();
            // assume argument for opt is a boolean 
            arg = "true";
        }
        return arg;
    }

    private static String stripLeadingHyphens(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("--")) {
            return str.substring(2, str.length());
        } else if (str.startsWith("-")) {
            return str.substring(1, str.length());
        }
        return str;
    }

    List<String> getArgs() {
        return arguments;
    }

    Map<String, String> getShortOpts() {
        return shortOpts;
    }

    String getShortOpt(String id) {
        return shortOpts.get(id);
    }

    Map<String, String> getLongOpts() {
        return longOpts;
    }

    String getLongOpt(String id) {
        return longOpts.get(id);
    }

    public static List<String> getReservedNonArgumentOptions() {
        return Arrays.asList(VERBOSE_SHORT_OPT, VERBOSE_SHORT_OPT, DEBUG_SHORT_OPT, DEBUG_LONG_OPT,
                HELP_SHORT_OPT, HELP_LONG_OPT);
    }

    public boolean verbose() {
        if (getShortOpt(VERBOSE_SHORT_OPT) != null || getLongOpt(VERBOSE_LONG_OPT) != null) {
            return true;
        }
        return false;
    }

    public boolean debug() {
        if (getShortOpt(DEBUG_SHORT_OPT) != null || getLongOpt(DEBUG_LONG_OPT) != null) {
            return true;
        }
        return false;
    }

    public boolean help() {
        if (getShortOpt(HELP_SHORT_OPT) != null || getLongOpt(HELP_LONG_OPT) != null) {
            return true;
        }
        return false;
    }

    public String toString() {
        return Objects.toStringHelper(GNUishParser.class).add("arguments", getArgs())
                .add("longOpts", longOpts).add("shortOpts", shortOpts).toString();

    }

}

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

/**
 * CliConsole is responsible for displaying information on the screen for the user that 
 * executes the command. 
 * 
 * CliConsole is not to be used for regular logging purposes, please use SLF4J for that.
 */
public class CliConsole {

    private boolean verbose = false;

    private CliConsole(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Create a console that is mostly silent, only producing required output. 
     * 
     * @return A console
     */
    static CliConsole createStandard() {
        return new CliConsole(false);
    }

    /**
     * Create a chatty console logger.
     * 
     * @return a chatty console 
     */
    static CliConsole createVerbose() {
        return new CliConsole(true);
    }

    // TOOD: May need a quiet console in future?
    // private boolean quiet = false;
    // static CliConsole createQuiet() {
    //   return new CliConsole(false);
    // }

    /**
     * If -v, --verbose is on
     */
    public void verbose(String msg) {
        if (!verbose) {
            return;
        }
        System.out.println(msg);
    }

    /**
     * Information to the user during regular execution. No output if -q,
     * --quiet is on.
     * 
     * @param msg
     */
    public void out(String msg) {
        System.out.println(msg);
    }
}

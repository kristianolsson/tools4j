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

import static org.deephacks.tools4j.cli.CliEvents.CLI001;
import static org.deephacks.tools4j.cli.CliEvents.CLI002;
import static org.deephacks.tools4j.cli.CliEvents.CLI003;
import static org.deephacks.tools4j.cli.CliEvents.CLI004;
import static org.deephacks.tools4j.cli.CliEvents.CLI101;
import static org.deephacks.tools4j.cli.CliEvents.CLI102;
import static org.deephacks.tools4j.cli.CliEvents.CLI104;
import static org.deephacks.tools4j.cli.CliEvents.CLI106;
import static org.deephacks.tools4j.cli.CliEvents.CLI107;
import static org.deephacks.tools4j.cli.CliEvents.CLI108;
import static org.deephacks.tools4j.cli.CliEvents.CLI109;
import static org.deephacks.tools4j.cli.CliEvents.CLI110;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.deephacks.tools4j.log.LogConfiguration;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.deephacks.tools4j.support.event.Event;
import org.deephacks.tools4j.support.lookup.MockLookup;
import org.deephacks.tools4j.support.test.JUnitUtils;
import org.deephacks.tools4j.support.types.DateTime;
import org.deephacks.tools4j.support.types.DurationTime;
import org.junit.Before;
import org.junit.Test;

public class CliMainTest {

    String stringValue = "string";
    String byteString = "1";
    Byte byteValue = new Byte(byteString);
    String integerString = "5";
    Integer integerValue = new Integer(integerString);
    String floatString = "5.0";
    Float floatValue = new Float(floatString);
    String doubleString = "1000000000000.0";
    Double doubleValue = new Double(doubleString);
    String longString = "1000000000000";
    Long longValue = new Long(longString);
    String shortString = "120";
    Short shortValue = new Short(shortString);
    String booleanString = "true";
    Boolean booleanValue = new Boolean(booleanString);
    String timeunitEnumString = "SECONDS";
    TimeUnit timeunitEnumValue = TimeUnit.SECONDS;
    String urlString = "http://www.test.com";
    URL urlValule = newURL(urlString);
    String fileString = ".";
    File fileValue = new File(fileString);
    String dateString = "2010-10-10";
    Date dateValue = new DateTime(dateString).parseDateTimeOrDate();
    String durationString = "PT1H2M20059S";
    DurationTime durationValue = new DurationTime(durationString);
    String dateTimeString = "2010-10-10T21:10:22";
    Date dateTimeValue = new DateTime(dateTimeString).parseDateTime();
    File cliArg1 = new File(".");
    Integer cliArg2 = 1;

    @Before
    public void before() {
        File mainDir = JUnitUtils.getMavenProjectChildFile(CliMainTest.class, "src/main");
        System.setProperty(Bootstrap.BOOT_DIR, ".");
        System.setProperty(Bootstrap.LIB_DIR, ".");
        LogConfiguration.init(new File(mainDir, "conf"));
        LogConfiguration.setDebug();
        System.setProperty(Bootstrap.CLI_HOME_VARIABLE, mainDir.getAbsolutePath());
    }

    @Test
    public void test_CLI001_SUCCESS() {
        TestCommand command = new TestCommand();
        MockLookup.setMockInstances(CliCommand.class, command);

        // command
        String[] args = new String[] { "commandword" };
        // short opts
        args = fill(args, new String[] { "-a", stringValue });
        args = fill(args, new String[] { "-b", byteString });
        args = fill(args, new String[] { "-c", integerString });
        args = fill(args, new String[] { "-e", floatString });
        args = fill(args, new String[] { "-f", doubleString });
        args = fill(args, new String[] { "-g", longString });
        // -h and --help is reserved for help 
        args = fill(args, new String[] { "-i", shortString });
        // non-argumented option
        args = fill(args, new String[] { "-j" });
        args = fill(args, new String[] { "-k", timeunitEnumString });
        args = fill(args, new String[] { "-l", urlString });
        args = fill(args, new String[] { "-m", fileString });
        args = fill(args, new String[] { "-o", durationString });
        args = fill(args, new String[] { "-p", dateTimeString });
        // turn on debug
        args = fill(args, new String[] { "-d" });
        // args
        args = fill(args, new String[] { "." });
        args = fill(args, new String[] { "1" });

        CliMain cli = new CliMain(args);

        Event event = cli.run();

        assertThat(event.getCode(), is(CLI001));
        assertThat(command.stringValue, is(stringValue));
        assertThat(command.byteValue, is(byteValue));
        assertThat(command.integerValue, is(integerValue));
        assertThat(command.floatValue, is(floatValue));
        assertThat(command.doubleValue, is(doubleValue));
        assertThat(command.longValue, is(longValue));
        assertThat(command.shortValue, is(shortValue));
        assertThat(command.booleanValue, is(booleanValue));
        assertThat(command.timeunitEnumValue, is(timeunitEnumValue));
        assertThat(command.urlValule, is(urlValule));
        assertThat(command.fileValue, is(fileValue));
        assertThat(command.durationValue, is(durationValue));
        assertThat(command.cliArg1.getAbsolutePath(), is(cliArg1.getAbsolutePath()));
        assertThat(command.cliArg2, is(cliArg2));

    }

    @Test
    public void test_CLI002_HELP_COMMAND() {
        TestCommand command = new TestCommand();
        MockLookup.setMockInstances(CliCommand.class, command);

        // short opt
        String[] args = new String[] { "commandword" };
        args = fill(args, new String[] { "-h" });
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI002));

        // long opt
        args = new String[] { "commandword" };
        args = fill(args, new String[] { "--help" });
        cli = new CliMain(args);
        event = cli.run();
        assertThat(event.getCode(), is(CLI002));

        // multi short opt
        args = new String[] { "commandword" };
        args = fill(args, new String[] { "-dh" });
        cli = new CliMain(args);
        event = cli.run();
        assertThat(event.getCode(), is(CLI002));
    }

    @Test
    public void test_CLI003_HELP_COMMANDS() {
        String[] args = new String[] { "" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI003));
    }

    @Test
    public void test_CLI004_UNEXPECTED_EXCEPTION() {
        @CliExtension(keyword = "unexpected", usage = "")
        final class UnexpectedExceptionCommand implements CliCommand {
            @Override
            public void execute(CliExecutionContext ctx) throws AbortRuntimeException {
                throw new RuntimeException("Unexpected Exception");
            }
        }
        ;

        MockLookup.setMockInstances(CliCommand.class, new UnexpectedExceptionCommand());
        String[] args = new String[] { "unexpected" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI004));
    }

    @Test
    public void test_CLI101_OPTION_INVALID_INPUT() {
        MockLookup.setMockInstances(CliCommand.class, new TestCommand());
        String[] args = new String[] { "commandword" };
        // try double value on integer
        args = fill(args, new String[] { "-c", doubleValue.toString() });
        CliMain cli = new CliMain(args);
        Event event = cli.run();

        assertThat(event.getCode(), is(CLI101));

    }

    @Test
    public void test_CLI102_ARGUMENT_INVALID_INPUT() {
        MockLookup.setMockInstances(CliCommand.class, new TestCommand());
        String[] args = new String[] { "commandword" };

        // try double value on integer
        args = fill(args, new String[] { "invalid_file" });
        args = fill(args, new String[] { "second_arg" });
        CliMain cli = new CliMain(args);
        Event event = cli.run();

        assertThat(event.getCode(), is(CLI102));

    }

    @Test
    public void test_CLI103_ARGUMENT_INPUT_MISSING() {
        // not implemented.. use 303 NotNull validation for this. 
    }

    @Test
    public void test_CLI104_INPUT_CONSTRAINT_VIOLATION() {
        @CliExtension(keyword = "validate", usage = "")
        final class ValidatedCommand implements CliCommand {
            @SuppressWarnings("unused")
            @CliOption(name = "opt", shortName = "o", desc = "desc")
            @Max(10)
            @Min(5)
            @NotNull
            private Integer opt;

            @SuppressWarnings("unused")
            @CliArgument(name = "arg", desc = "desc", position = 0)
            @Max(10)
            @Min(5)
            @NotNull
            private Integer arg;

            @Override
            public void execute(CliExecutionContext ctx) throws AbortRuntimeException {

            }

        }
        ;
        MockLookup.setMockInstances(CliCommand.class, new ValidatedCommand());

        // success
        String[] args = new String[] { "validate" };
        args = fill(args, new String[] { "-o", "6" });
        args = fill(args, new String[] { "-d" });
        args = fill(args, new String[] { "6" });
        CliMain cli = new CliMain(args);
        Event event = cli.run();

        assertThat(event.getCode(), is(CLI001));

        // too small opt
        args = new String[] { "validate" };
        args = fill(args, new String[] { "-o", "4" });
        args = fill(args, new String[] { "6" });
        cli = new CliMain(args);
        event = cli.run();

        assertThat(event.getCode(), is(CLI104));

        // too small arg
        args = new String[] { "validate" };
        args = fill(args, new String[] { "-o", "6" });
        args = fill(args, new String[] { "1" });
        cli = new CliMain(args);
        event = cli.run();

    }

    /**
     * TODO: This should be handled at compile time by an 
     * annotation processor.
     */
    @Test
    public void test_CLI105_ARG_POSITION_MISSING() {
    }

    /**
     * TODO: This should be handled at compile time by an 
     * annotation processor.
     */
    @Test
    public void test_CLI106_DUPLICATE_ARG_OPT_ID() {
        @CliExtension(keyword = "duplicate", usage = "")
        class DuplicateOptArgCommand implements CliCommand {
            @SuppressWarnings("unused")
            @CliOption(name = "arg", desc = "desc", shortName = "o")
            private Integer arg;

            @SuppressWarnings("unused")
            @CliOption(name = "arg", desc = "desc", shortName = "o")
            private Integer opt;

            @Override
            public void execute(CliExecutionContext ctx) throws AbortRuntimeException {

            }
        }
        ;

        MockLookup.setMockInstances(CliCommand.class, new DuplicateOptArgCommand());
        String[] args = new String[] { "duplicate" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();

        assertThat(event.getCode(), is(CLI106));

    }

    /**
     * TODO: This should be handled at compile time by an 
     * annotation processor.
     */
    @Test
    public void test_CLI107_RESERVED_ARG_OPT_NAME() {
        @SuppressWarnings("hiding")
        @CliExtension(keyword = "test", usage = "")
        final class TestCommand implements CliCommand {
            @SuppressWarnings("unused")
            @CliOption(name = "v", desc = "desc", shortName = "v")
            private Integer arg;

            @Override
            public void execute(CliExecutionContext ctx) throws AbortRuntimeException {

            }
        }
        ;
        MockLookup.setMockInstances(CliCommand.class, new TestCommand());
        String[] args = new String[] { "test" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI107));

    }

    @Test
    public void test_CLI108_DUPLICATE_COMMANDS() {
        @SuppressWarnings("hiding")
        @CliExtension(keyword = "test", usage = "")
        final class TestCommand implements CliCommand {
            @SuppressWarnings("unused")
            @CliOption(name = "v", desc = "desc", shortName = "o")
            private Integer opt;

            @Override
            public void execute(CliExecutionContext ctx) throws AbortRuntimeException {

            }
        }
        ;
        MockLookup.setMockInstances(CliCommand.class, new TestCommand(), new TestCommand());
        String[] args = new String[] { "test" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI108));

    }

    @Test
    public void test_CLI109_COMMAND_DOES_NOT_EXIST() {
        MockLookup.setMockInstances(CliCommand.class, new TestCommand());
        String[] args = new String[] { "bogus" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI109));

    }

    @Test
    public void test_CLI110_CONF_DOES_NOT_EXIST() {
        System.setProperty(Bootstrap.CLI_HOME_VARIABLE, "bogus_directory");

        MockLookup.setMockInstances(CliCommand.class, new TestCommand());
        String[] args = new String[] { "commandword" };
        CliMain cli = new CliMain(args);
        Event event = cli.run();
        assertThat(event.getCode(), is(CLI110));
    }

    private static URL newURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    String[] fill(String[] first, String[] second) {
        List<String> both = new ArrayList<String>(first.length + second.length);
        Collections.addAll(both, first);
        Collections.addAll(both, second);
        return both.toArray(new String[] {});
    }

    @CliExtension(keyword = "commandword", usage = "Some usage of this command")
    public class TestCommand implements CliCommand {
        @CliOption(name = "aa", shortName = "a", desc = "")
        private String stringValue;
        @CliOption(name = "bb", shortName = "b", desc = "")
        private Byte byteValue;
        @CliOption(name = "cc", shortName = "c", desc = "")
        private Integer integerValue;
        @CliOption(name = "ee", shortName = "e", desc = "")
        private Float floatValue;
        @CliOption(name = "ff", shortName = "f", desc = "")
        private Double doubleValue;
        @CliOption(name = "gg", shortName = "g", desc = "")
        private Long longValue;
        @CliOption(name = "ii", shortName = "i", desc = "")
        private Short shortValue;
        @CliOption(name = "jj", shortName = "j", desc = "")
        private Boolean booleanValue;
        @CliOption(name = "kk", shortName = "k", desc = "")
        private TimeUnit timeunitEnumValue;
        @CliOption(name = "ll", shortName = "l", desc = "")
        private URL urlValule;
        @CliOption(name = "mm", shortName = "m", desc = "")
        private File fileValue;
        @CliOption(name = "oo", shortName = "o", desc = "")
        private DurationTime durationValue;
        @CliArgument(name = "cliarg1", position = 0, desc = "")
        private File cliArg1;
        @CliArgument(name = "cliarg2", position = 1, desc = "")
        private Integer cliArg2;

        @Override
        public void execute(CliExecutionContext context) {
        }
    }

}

/**F
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

import static org.deephacks.tools4j.cli.CliEvents.CLI101_OPTION_INVALID_INPUT;
import static org.deephacks.tools4j.cli.CliEvents.CLI102_ARGUMENT_INVALID_INPUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.ConversionException;
import org.deephacks.tools4j.support.reflections.BeanAnnotatedField;
import org.deephacks.tools4j.support.reflections.BeanInstance;

/**
 * This class is responsible for mapping terminal args to the metadata model.
 */
class CliCommandInjector {
    private GNUishParser parser;
    private BeanInstance<CliCommand> target;
    private static Conversion CONVERSION = Conversion.get();

    CliCommandInjector(GNUishParser parser, BeanInstance<CliCommand> target) {
        this.parser = parser;
        this.target = target;
    }

    void inject() {
        target.injectFieldsAnnotatedWith(CliArgument.class).withValues(parsedCommandArguments());
        target.injectFieldsAnnotatedWith(CliOption.class).withValues(parsedCommandOptions());
    }

    /**
     * Map the parsed arguments to position of the CliArgument annotation. 
     */
    private Map<String, Object> parsedCommandArguments() {
        Map<String, Object> mapper = new HashMap<String, Object>();
        List<String> args = parser.getArgs();
        Map<String, BeanAnnotatedField<CliArgument>> fieldArgs = target
                .findFieldsAnnotatedWith(CliArgument.class);
        if (args == null || args.size() == 0) {
            return mapper;
        }
        for (BeanAnnotatedField<CliArgument> commandField : fieldArgs.values()) {
            // the position in parser and annotation should be same.
            String argInput = args.get(commandField.getAnnotation().position());
            String fieldName = commandField.getName();
            try {
                Object o = CONVERSION.convert(argInput, commandField.getType());
                mapper.put(fieldName, o);
            } catch (ConversionException e) {
                throw CLI102_ARGUMENT_INVALID_INPUT(commandField);
            }
        }
        return mapper;
    }

    private Map<String, Object> parsedCommandOptions() {
        Map<String, Object> mapper = new HashMap<String, Object>();

        Map<String, BeanAnnotatedField<CliOption>> commandOptions = target
                .findFieldsAnnotatedWith(CliOption.class);
        for (BeanAnnotatedField<CliOption> commandField : commandOptions.values()) {
            // first long opts
            String longOptInput = parser.getLongOpt(commandField.getAnnotation().name());
            if (longOptInput != null || !"".equals(longOptInput)) {
                String fieldName = commandField.getName();
                mapper.put(fieldName, CONVERSION.convert(longOptInput, commandField.getType()));
            }
            // second short opts
            String shortOptInput = parser.getShortOpt(commandField.getAnnotation().shortName());
            if (longOptInput != null || !"".equals(shortOptInput)) {
                String fieldName = commandField.getName();
                try {
                    Object v = CONVERSION.convert(shortOptInput, commandField.getType());
                    mapper.put(fieldName, v);
                } catch (ConversionException e) {
                    e.printStackTrace();
                    throw CLI101_OPTION_INVALID_INPUT(commandField);
                }
            }
        }

        return mapper;
    }

}

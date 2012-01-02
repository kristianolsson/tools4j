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

import static org.deephacks.tools4j.cli.CliEvents.CLI104_INPUT_CONSTRAINT_VIOLATION;
import static org.deephacks.tools4j.cli.CliEvents.CLI105_ARG_POSITION_MISSING;
import static org.deephacks.tools4j.cli.CliEvents.CLI106_DUPLICATE_ARG_OPT_ID;
import static org.deephacks.tools4j.cli.CliEvents.CLI107_RESERVED_ARG_OPT_NAME;
import static org.deephacks.tools4j.cli.CliEvents.CLI108_DUPLICATE_COMMANDS;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.deephacks.tools4j.support.reflections.BeanAnnotatedField;
import org.deephacks.tools4j.support.reflections.BeanInstance;
import org.deephacks.tools4j.support.types.DurationTime;

import com.google.common.collect.Maps;

class CliExtensionValidator {
    private BeanInstance<CliCommand> cliCommand;

    CliExtensionValidator(BeanInstance<CliCommand> command) {
        this.cliCommand = command;
    }

    void validateCliOptionMetadata() {
        Map<String, BeanAnnotatedField<CliOption>> optionsIndexedOnShortAndLongName = Maps
                .newHashMap();
        Map<String, BeanAnnotatedField<CliOption>> fields = cliCommand
                .findFieldsAnnotatedWith(CliOption.class);
        String cliExtensionName = cliCommand.getClassAnnotation(CliExtension.class).keyword();
        for (BeanAnnotatedField<CliOption> field : fields.values()) {
            String shortName = field.getAnnotation().shortName();
            String name = field.getAnnotation().name();
            if (optionsIndexedOnShortAndLongName.get(shortName) != null) {
                throw CLI106_DUPLICATE_ARG_OPT_ID(cliExtensionName);

            }
            if (optionsIndexedOnShortAndLongName.get(name) != null) {
                throw CLI108_DUPLICATE_COMMANDS(cliExtensionName);
            }
            if ("v".equals(shortName) || "d".equals(shortName) || "debug".equals(name)
                    || "verbose".equals(name)) {
                throw CLI107_RESERVED_ARG_OPT_NAME(cliExtensionName);
            }
            optionsIndexedOnShortAndLongName.put(shortName, field);
            optionsIndexedOnShortAndLongName.put(name, field);
        }
    }

    void validateCliArgumentMetadata() {
        // validate the numerical sequence of argument positions
        Map<String, BeanAnnotatedField<CliArgument>> arguments = cliCommand
                .findFieldsAnnotatedWith(CliArgument.class);
        Map<Integer, Integer> positions = new HashMap<Integer, Integer>();
        for (BeanAnnotatedField<CliArgument> argument : arguments.values()) {
            positions.put(argument.getAnnotation().position(), argument.getAnnotation().position());
        }
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) == null) {
                throw CLI105_ARG_POSITION_MISSING(cliCommand.getClassAnnotation(CliExtension.class)
                        .keyword(), (i + 1));
            }
        }

    }

    void validateCommandInput() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CliCommand>> violations = validator.validate(cliCommand.get());
        if (violations.size() != 0) {
            for (ConstraintViolation<CliCommand> constraintViolation : violations) {
                Map<String, BeanAnnotatedField<CliOption>> fields = cliCommand
                        .findFieldsAnnotatedWith(CliOption.class);
                BeanAnnotatedField<CliOption> violatedField = fields.get(constraintViolation
                        .getPropertyPath().toString());

                if (violatedField != null) {
                    String name = violatedField.getAnnotation().shortName();
                    String msg = constraintViolation.getMessage();
                    throw CLI104_INPUT_CONSTRAINT_VIOLATION(name, msg);
                }
                Map<String, BeanAnnotatedField<CliArgument>> argFields = cliCommand
                        .findFieldsAnnotatedWith(CliArgument.class);
                BeanAnnotatedField<CliArgument> violatedArgFields = argFields
                        .get(constraintViolation.getPropertyPath().toString());
                if (violatedArgFields != null) {
                    String name = violatedArgFields.getAnnotation().name();
                    String msg = constraintViolation.getMessage();
                    throw CLI104_INPUT_CONSTRAINT_VIOLATION(name, msg);
                }

            }
        }

    }

    static String getInputFormatString(Class<?> type) {
        if (Boolean.class.isAssignableFrom(type)) {
            // booleans doesnt have arguments
            return "";
        }
        if (Double.class.isAssignableFrom(type)) {
            return "decimal";
        }
        if (Number.class.isAssignableFrom(type)) {
            return "number";
        }
        if (Date.class.isAssignableFrom(type)) {
            return "date";
        }
        if (String.class.isAssignableFrom(type)) {
            return "str";
        }
        if (File.class.isAssignableFrom(type)) {
            return "file";
        }
        if (URL.class.isAssignableFrom(type)) {
            return "url";
        }
        if (DurationTime.class.isAssignableFrom(type)) {
            return "duration[PTnHnMnS]";
        }
        if (Collection.class.isAssignableFrom(type)) {
            return "list";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "key=value";
        }
        if (Enum.class.isAssignableFrom(type)) {
            return getEnumFormatString(type);
        }

        return type.getCanonicalName();
    }

    private static String getEnumFormatString(Class<?> clazz) {
        StringBuffer sb = new StringBuffer();
        Field[] fields = clazz.getDeclaredFields();
        List<String> values = new ArrayList<String>();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEnumConstant()) {
                try {
                    Object aEnum = fields[i].get(null);
                    values.add(aEnum.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.get(i));
            if ((i + 1) != values.size()) {
                sb.append("|");
            }
        }
        return sb.toString();
    }
}

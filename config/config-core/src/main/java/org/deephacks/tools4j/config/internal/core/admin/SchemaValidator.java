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
package org.deephacks.tools4j.config.internal.core.admin;

import static org.deephacks.tools4j.support.reflections.Reflections.forName;

import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.support.conversion.Conversion;

public class SchemaValidator {
    private static Conversion conversion = Conversion.get();

    /**
     * Validate that the value of the bean is according to schema.
     */
    public static void validateSchema(Collection<Bean> beans) {
        for (Bean bean : beans) {
            validateSchema(bean);
        }
    }

    /**
     * Validate that the value of the bean is according to schema. 
     */
    public static void validateSchema(Bean bean) {
        Schema schema = bean.getSchema();

        if (bean.getId().getInstanceId() == null || "".equals(bean.getId().getInstanceId())) {
            throw Events.CFG107_MISSING_ID();
        }
        for (SchemaProperty prop : schema.get(SchemaProperty.class)) {
            List<String> values = bean.getValues(prop.getName());
            if (values == null) {
                continue;
            }
            if (values.size() > 1) {
                throw Events.CFG106_WRONG_MULTIPLICITY_TYPE(bean.getId(), prop.getName());
            }
            String value = values.get(0);

            try {
                conversion.convert(value, forName(prop.getType()));
            } catch (Exception e) {
                throw Events.CFG105_WRONG_PROPERTY_TYPE(bean.getId(), prop.getName(),
                        prop.getType(), value);
            }
        }
        for (SchemaPropertyList prop : schema.get(SchemaPropertyList.class)) {
            List<String> values = bean.getValues(prop.getName());
            if (values == null) {
                continue;
            }
            for (String value : values) {
                try {
                    conversion.convert(value, forName(prop.getType()));
                } catch (Exception e) {
                    throw Events.CFG105_WRONG_PROPERTY_TYPE(bean.getId(), prop.getName(),
                            prop.getType(), value);
                }
            }
        }

    }
}

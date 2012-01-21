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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.AbstractSchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;
import org.deephacks.tools4j.config.spi.ClassRepository;
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
        ClassRepository repos = new ClassRepository();
        Schema schema = bean.getSchema();

        if (bean.getId().getInstanceId() == null || "".equals(bean.getId().getInstanceId())) {
            throw Events.CFG107_MISSING_ID();
        }
        Set<String> schemaPropertyNames = schema.getPropertyNames();
        for (String name : bean.getPropertyNames()) {
            if (!schemaPropertyNames.contains(name)) {
                throw Events.CFG110_PROP_NOT_EXIST_IN_SCHEMA(name);
            }
        }
        Set<String> schemaReferenceNames = schema.getReferenceNames();
        for (String name : bean.getReferenceNames()) {
            if (!schemaReferenceNames.contains(name)) {
                throw Events.CFG111_REF_NOT_EXIST_IN_SCHEMA(name);
            }
        }
        for (SchemaProperty prop : schema.get(SchemaProperty.class)) {
            String value = validateSingle(bean, prop);
            if (value == null) {
                return;
            }

            try {
                conversion.convert(value, repos.loadClass(prop.getType()));
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
                    conversion.convert(value, repos.loadClass(prop.getType()));
                } catch (Exception e) {
                    throw Events.CFG105_WRONG_PROPERTY_TYPE(bean.getId(), prop.getName(),
                            prop.getType(), value);
                }
            }
        }
        for (SchemaPropertyRef prop : schema.get(SchemaPropertyRef.class)) {
            validateSingle(bean, prop);
        }
        for (SchemaPropertyRefList prop : schema.get(SchemaPropertyRefList.class)) {
        }
        for (SchemaPropertyRefMap prop : schema.get(SchemaPropertyRefMap.class)) {
        }

    }

    private static String validateSingle(Bean bean, AbstractSchemaProperty prop) {
        List<String> values = bean.getValues(prop.getName());
        if (values == null) {
            return null;
        }
        if (prop.isImmutable()) {
            throw Events.CFG306_PROPERTY_IMMUTABLE(bean.getId(), prop.getName());
        }
        if (values.size() > 1) {
            throw Events.CFG106_WRONG_MULTIPLICITY_TYPE(bean.getId(), prop.getName());
        }
        return values.get(0);
    }
}

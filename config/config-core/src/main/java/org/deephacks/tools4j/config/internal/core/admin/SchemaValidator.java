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
        validateId(bean);
        validatePropertyNames(bean);
        validateReferences(bean);
        validateProperties(bean);
        validatePropertyList(bean);
        validatePropertyReferences(bean);
        validatePropertyRefList(bean);
        validatePropertyRefMap(bean);
    }

    private static void validateId(Bean bean) {
        if (bean.getId().getInstanceId() == null || "".equals(bean.getId().getInstanceId())) {
            throw Events.CFG107_MISSING_ID();
        }
    }

    private static void validatePropertyRefMap(Bean bean) {
        Schema schema = bean.getSchema();
        for (SchemaPropertyRefMap prop : schema.get(SchemaPropertyRefMap.class)) {
        }
    }

    private static void validatePropertyRefList(Bean bean) {
        Schema schema = bean.getSchema();
        for (SchemaPropertyRefList prop : schema.get(SchemaPropertyRefList.class)) {
        }
    }

    private static void validatePropertyReferences(Bean bean) {
        Schema schema = bean.getSchema();
        for (SchemaPropertyRef prop : schema.get(SchemaPropertyRef.class)) {
            validateSingle(bean, prop);
        }
    }

    private static void validatePropertyList(Bean bean) {
        Schema schema = bean.getSchema();
        ClassRepository repos = new ClassRepository();
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
    }

    private static void validateProperties(Bean bean) {
        Schema schema = bean.getSchema();
        ClassRepository repos = new ClassRepository();
        for (SchemaProperty prop : schema.get(SchemaProperty.class)) {
            String value = validateSingle(bean, prop);
            if (value == null) {
                continue;
            }

            try {
                conversion.convert(value, repos.loadClass(prop.getType()));
            } catch (Exception e) {
                throw Events.CFG105_WRONG_PROPERTY_TYPE(bean.getId(), prop.getName(),
                        prop.getType(), value);
            }
        }
    }

    private static void validateReferences(Bean bean) {
        Schema schema = bean.getSchema();
        Set<String> schemaReferenceNames = schema.getReferenceNames();
        for (String name : bean.getReferenceNames()) {
            if (!schemaReferenceNames.contains(name)) {
                throw Events.CFG111_REF_NOT_EXIST_IN_SCHEMA(name);
            }
        }
    }

    private static void validatePropertyNames(Bean bean) {
        Schema schema = bean.getSchema();
        Set<String> schemaPropertyNames = schema.getPropertyNames();
        for (String name : bean.getPropertyNames()) {
            if (!schemaPropertyNames.contains(name)) {
                throw Events.CFG110_PROP_NOT_EXIST_IN_SCHEMA(name);
            }
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

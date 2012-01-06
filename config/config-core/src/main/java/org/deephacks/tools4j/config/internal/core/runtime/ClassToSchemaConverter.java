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
package org.deephacks.tools4j.config.internal.core.runtime;

import static org.deephacks.tools4j.config.model.Events.CFG102_NOT_CONFIGURABLE;
import static org.deephacks.tools4j.config.model.Events.CFG103_NO_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.Property;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.AbstractSchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaId;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.conversion.Converter;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

public class ClassToSchemaConverter implements Converter<Class<?>, Schema> {
    private Conversion conversion = Conversion.get();

    @Override
    public Schema convert(Class<?> source, Class<? extends Schema> specificType) {
        ClassIntrospector introspector = new ClassIntrospector(source);
        Config config = introspector.get(Config.class);
        if (config == null) {
            throw CFG102_NOT_CONFIGURABLE(source);
        }
        SchemaId schemaId = getId(introspector);
        Schema schema = Schema.create(schemaId, introspector.getName(), config.name(),
                config.desc(), config.multiplicity().toString());
        Collection<Object> fields = new ArrayList<Object>();
        fields.addAll(introspector.getFieldList(Property.class));
        Collection<AbstractSchemaProperty> props = conversion.convert(fields,
                AbstractSchemaProperty.class);
        for (AbstractSchemaProperty abstractProp : props) {
            schema.add(abstractProp);
        }
        return schema;
    }

    private SchemaId getId(ClassIntrospector introspector) {
        List<FieldWrap<Id>> id = introspector.getFieldList(Id.class);
        if (id == null || id.size() == 0) {
            throw CFG103_NO_ID(introspector.getTarget());
        }
        FieldWrap<Id> idField = id.get(0);
        SchemaId schemaId = SchemaId.create(idField.getAnnotation().name(), idField.getAnnotation()
                .desc());
        return schemaId;
    }
}

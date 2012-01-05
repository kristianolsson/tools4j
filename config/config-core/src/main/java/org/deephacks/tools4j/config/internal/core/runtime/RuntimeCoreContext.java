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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.lookup.Lookup;

import com.google.common.collect.Lists;

/**
 * RuntimeCoreContext is responsible for separating the admin, runtime and spi 
 * context so that no dependencies (compile nor runtime) exist between them.
 */
public class RuntimeCoreContext extends RuntimeContext {
    private Conversion conversion = Conversion.get();
    private SchemaManager schemaManager;
    private BeanManager beanManager;

    public RuntimeCoreContext() {
        conversion.register(new ClassToSchemaConverter());
        conversion.register(new FieldToSchemaPropertyConverter());
        conversion.register(new BeanToObjectConverter());
        schemaManager = Lookup.get().lookup(SchemaManager.class);
        beanManager = Lookup.get().lookup(BeanManager.class);
    }

    @Override
    public void register(Class<?> configurable) {
        Schema schema = conversion.convert(configurable, Schema.class);
        ArrayList<Schema> schemas = new ArrayList<Schema>(Arrays.asList(schema));
        schemaManager.addSchemas(schemas);
    }

    @Override
    public void unregister(Class<?> configurable) {
        Schema schema = conversion.convert(configurable, Schema.class);
        schemaManager.removeSchema(schema.getName());
    }

    @Override
    public <T> T singleton(Class<T> clazz) {
        Bean bean = beanManager.get(BeanId.create(clazz.getName(), clazz.getName()));
        SchemaManager schemaManager = Lookup.get().lookup(SchemaManager.class);
        Map<String, Schema> schemas = schemaManager.schemaMap();
        setSchema(bean, schemas);
        return conversion.convert(bean, clazz);
    }

    @Override
    public <T> List<T> all(Class<T> clazz) {
        Schema s = schemaManager.getSchema(clazz.getAnnotation(Config.class).name());
        Map<String, Schema> schemas = schemaManager.schemaMap();
        Map<BeanId, Bean> beans = beanManager.list(s.getName());
        setSchema(beans, schemas);
        return Lists.newArrayList(conversion.convert(beans.values(), clazz));
    }

    @Override
    public <T> T get(String id, Class<T> clazz) {
        Schema s = schemaManager.getSchema(clazz.getAnnotation(Config.class).name());
        Map<String, Schema> schemas = schemaManager.schemaMap();
        BeanId beanId = BeanId.create(id, s.getName());
        Bean bean = beanManager.get(beanId);
        if (bean == null) {
            throw Events.CFG304_BEAN_DOESNT_EXIST(beanId);
        }
        setSchema(bean, schemas);
        return conversion.convert(bean, clazz);
    }

    private static void setSchema(Bean b, Map<String, Schema> schemas) {
        List<String> names = b.getReferenceNames();
        for (String name : names) {
            for (BeanId id : b.getReference(name)) {
                Bean ref = id.getBean();
                if (ref != null) {
                    setSchema(ref, schemas);
                }
            }
        }
        Schema s = schemas.get(b.getId().getSchemaName());
        if (s == null) {
            throw new UnsupportedOperationException(
                    "Schema must always be available for any beans. This is a programming error/bug.");
        }
        b.set(s);
    }

    private static void setSchema(Map<BeanId, Bean> beans, Map<String, Schema> schemas) {
        for (Bean b : beans.values()) {
            setSchema(b, schemas);
        }
    }

}

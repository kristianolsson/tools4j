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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.lookup.Lookup;
import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;

import com.google.common.collect.Lists;

/**
 * RuntimeCoreContext is responsible for separating the admin, runtime and spi 
 * context so that no dependencies (compile nor runtime) exist between them.
 */
public class RuntimeCoreContext extends RuntimeContext {
    private Conversion conversion = Conversion.get();
    private SchemaManager schemaManager;
    private BeanManager beanManager;
    private ValidationManager validationManager;

    public RuntimeCoreContext() {
        conversion.register(new ClassToSchemaConverter());
        conversion.register(new FieldToSchemaPropertyConverter());
        conversion.register(new BeanToObjectConverter());
        schemaManager = Lookup.get().lookup(SchemaManager.class);
        beanManager = lookupBeanManager();
        validationManager = Lookup.get().lookup(ValidationManager.class);
    }

    @Override
    public void register(Class<?>... configurable) {
        for (Class<?> clazz : configurable) {
            Schema schema = conversion.convert(clazz, Schema.class);
            schemaManager.regsiterSchema(schema);
            if (schema.getId().isSingleton()) {
                beanManager.createSingleton(getSingletonId(schema, clazz));
            }
            // ok to not have validation manager available
            if (validationManager != null) {
                validationManager.register(schema.getName(), clazz);
            }
        }
    }

    @Override
    public void unregister(Class<?>... configurable) {
        for (Class<?> clazz : configurable) {
            Schema schema = conversion.convert(clazz, Schema.class);
            schemaManager.removeSchema(schema.getName());
            // ok to not have validation manager available
            if (validationManager != null) {
                validationManager.unregister(schema.getName());
            }
        }
    }

    @Override
    public <T> T singleton(Class<T> configurable) {
        Schema schema = conversion.convert(configurable, Schema.class);
        BeanId singleton = getSingletonId(schema, configurable);
        Map<String, Schema> schemas = schemaManager.getSchemas();
        Bean bean = beanManager.getEager(singleton);
        bean.set(schema);
        setSingletonReferences(bean, schemas);
        return conversion.convert(bean, configurable);
    }

    @Override
    public <T> List<T> all(Class<T> clazz) {
        Schema s = schemaManager.getSchema(clazz.getAnnotation(Config.class).name());
        Map<String, Schema> schemas = schemaManager.getSchemas();
        Map<BeanId, Bean> beans = beanManager.list(s.getName());
        setSchema(beans, schemas);
        for (Bean bean : beans.values()) {
            setSingletonReferences(bean, schemas);
        }
        return Lists.newArrayList(conversion.convert(beans.values(), clazz));
    }

    @Override
    public <T> T get(String id, Class<T> clazz) {
        Schema s = schemaManager.getSchema(clazz.getAnnotation(Config.class).name());
        Map<String, Schema> schemas = schemaManager.getSchemas();
        BeanId beanId = BeanId.create(id, s.getName());
        Bean bean = beanManager.getEager(beanId);
        if (bean == null) {
            throw Events.CFG304_BEAN_DOESNT_EXIST(beanId);
        }
        setSchema(bean, schemas);
        setSingletonReferences(bean, schemas);
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

    private BeanId getSingletonId(Schema s, Class<?> configurable) {
        try {
            ClassIntrospector introspector = new ClassIntrospector(configurable);
            FieldWrap<Id> id = introspector.getFieldList(Id.class).get(0);
            String instanceId = id.getStaticValue().toString();
            return BeanId.createSingleton(instanceId, s.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException("Configurable class [" + configurable
                    + "] is not a singleton.", e);
        }
    }

    private void setSingletonReferences(Bean bean, Map<String, Schema> schemas) {
        Schema s = bean.getSchema();
        for (SchemaPropertyRef ref : s.get(SchemaPropertyRef.class)) {
            if (ref.isSingleton()) {
                Schema singletonSchema = schemas.get(ref.getSchemaName());
                Bean singleton = beanManager.getSingleton(ref.getSchemaName());
                singleton.set(singletonSchema);
                BeanId singletonId = singleton.getId();
                singletonId.setBean(singleton);
                // recursive call.
                setSingletonReferences(singleton, schemas);
                bean.setReference(ref.getName(), singletonId);
            }
        }
    }

    private static BeanManager lookupBeanManager() {
        Collection<BeanManager> beanManagers = Lookup.get().lookupAll(BeanManager.class);
        if (beanManagers.size() == 1) {
            return beanManagers.iterator().next();
        }
        String preferedBeanManager = SystemProperties.createDefault().get("config.beanmanager");
        if (preferedBeanManager == null || "".equals(preferedBeanManager)) {
            return beanManagers.iterator().next();
        }
        for (BeanManager beanManager : beanManagers) {
            if (beanManager.getClass().getName().equals(preferedBeanManager)) {
                return beanManager;
            }
        }
        return new XmlBeanManager();
    }
}

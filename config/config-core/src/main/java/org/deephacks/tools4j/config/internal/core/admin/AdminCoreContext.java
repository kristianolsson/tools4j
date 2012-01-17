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

import static org.deephacks.tools4j.config.internal.core.admin.SchemaValidator.validateSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.runtime.BeanToObjectConverter;
import org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.BeanUtils;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.config.spi.ValidationManager;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.lookup.Lookup;

/**
 * AdminCoreContext is responsible for separating the admin and runtime 
 * context so that no dependencies (compile nor runtime) exist between them.
 *   
 */
public class AdminCoreContext extends AdminContext {
    private BeanManager beanManager;
    private SchemaManager schemaManager;
    private ValidationManager validationManager;
    private Conversion conversion = Conversion.get();

    public AdminCoreContext() {
        beanManager = lookupBeanManager();
        schemaManager = Lookup.get().lookup(SchemaManager.class);
        validationManager = Lookup.get().lookup(ValidationManager.class);
        conversion.register(new BeanToObjectConverter());

    }

    @Override
    public List<Bean> list(String schemaName) {
        Map<BeanId, Bean> beans = beanManager.list(schemaName);
        Map<String, Schema> schemas = schemaManager.getSchemas();
        setSchema(schemas, beans);
        return new ArrayList<Bean>(beans.values());
    }

    @Override
    public List<Bean> list(String schemaName, Collection<String> instanceIds) {
        Map<BeanId, Bean> beans = beanManager.list(schemaName);
        Map<BeanId, Bean> result = new HashMap<BeanId, Bean>();
        for (String instanceId : instanceIds) {
            Bean b = beans.get(BeanId.create(instanceId, schemaName));
            if (b == null) {
                // TODO: FIX THIS METHOD
            }
            result.put(b.getId(), b);
        }
        Map<String, Schema> schemas = schemaManager.getSchemas();
        setSchema(schemas, result);

        return new ArrayList<Bean>(result.values());

    }

    @Override
    public Bean get(BeanId beanId) {
        Map<String, Schema> schemas = schemaManager.getSchemas();
        Bean bean = beanManager.get(beanId);
        bean.set(schemas.get(beanId.getSchemaName()));
        setSchema(schemaManager.getSchemas(), bean);
        setSingletonReferences(bean, schemas);
        return bean;
    }

    @Override
    public void create(Bean bean) {
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        bean.set(schema);
        validateSchema(bean);
        // ok to not have validation manager available
        if (validationManager != null) {
            initalizeReferences(bean);
            validationManager.validate(Arrays.asList(bean));
        }
        beanManager.create(bean);
    }

    @Override
    public void create(Collection<Bean> beans) {
        Map<String, Schema> schemas = schemaManager.getSchemas();
        setSchema(schemas, beans);
        validateSchema(beans);
        // ok to not have validation manager available
        if (validationManager != null) {
            initalizeReferences(beans);
            validationManager.validate(beans);
        }
        beanManager.create(beans);
    }

    @Override
    public void set(Bean bean) {
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        bean.set(schema);
        validateSchema(bean);
        // ok to not have validation manager available
        if (validationManager != null) {
            initalizeReferences(bean);
            validationManager.validate(Arrays.asList(bean));
        }
        beanManager.set(bean);
    }

    @Override
    public void set(Collection<Bean> beans) {
        Map<String, Schema> schemas = schemaManager.getSchemas();
        setSchema(schemas, beans);
        validateSchema(beans);
        // ok to not have validation manager available
        if (validationManager != null) {
            initalizeReferences(beans);
            validationManager.validate(beans);
        }
        beanManager.set(beans);
    }

    @Override
    public void merge(Bean bean) {
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        bean.set(schema);
        validateSchema(bean);
        // ok to not have validation manager available
        if (validationManager != null) {
            Bean source = beanManager.get(bean.getId());
            if (source == null) {
                source = bean;
            } else {
                setSchema(schemaManager.getSchemas(), source);
                merge(source, bean);
            }
            validationManager.validate(Arrays.asList(source));
        }
        beanManager.merge(bean);
    }

    private void merge(Bean source, Bean mergeBean) {
        for (String name : mergeBean.getPropertyNames()) {
            List<String> values = mergeBean.getValues(name);
            source.setProperty(name, values);
        }
        initalizeReferences(mergeBean);
    }

    @Override
    public void merge(Collection<Bean> beans) {
        ArrayList<Bean> merges = new ArrayList<Bean>();
        for (Bean bean : beans) {
            Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
            bean.set(schema);
            merges.add(bean);
        }
        validateSchema(merges);
        // ok to not have validation manager available
        if (validationManager != null) {
            for (Bean bean : merges) {
                Bean source = beanManager.get(bean.getId());
                if (source == null) {
                    source = bean;
                } else {
                    setSchema(schemaManager.getSchemas(), source);
                    merge(source, bean);
                }
                validationManager.validate(Arrays.asList(source));
            }
        }
        beanManager.merge(merges);
    }

    @Override
    public void delete(BeanId beanId) {
        beanManager.delete(beanId);
    }

    @Override
    public void delete(String name, Collection<String> instances) {
        beanManager.delete(name, instances);
    }

    /**
     * Used for setting or creating a single bean.
     */
    private void initalizeReferences(Bean bean) {
        for (String name : bean.getReferenceNames()) {
            List<BeanId> values = bean.getReference(name);
            if (values == null) {
                continue;
            }
            for (BeanId beanId : values) {
                Bean ref = beanManager.get(beanId);
                beanId.setBean(ref);
                setSchema(schemaManager.getSchemas(), beanId.getBean());
            }
        }
    }

    /**
     * Used for setting or creating a multiple beans. 
     * 
     * We must consider that the operation may include beans that have 
     * references betewen eachother. User provided beans are 
     * prioritized and the storage is secondary for looking up references.  
     */
    private void initalizeReferences(Collection<Bean> beans) {
        Map<BeanId, Bean> map = BeanUtils.uniqueIndex(beans);
        for (Bean bean : beans) {
            for (String name : bean.getReferenceNames()) {
                List<BeanId> values = bean.getReference(name);
                if (values == null) {
                    continue;
                }
                for (BeanId beanId : values) {
                    // the does not exist in storage, but may exist in the
                    // set of beans provided by the user.
                    Bean ref = map.get(beanId);
                    if (ref == null) {
                        ref = beanManager.get(beanId);
                    }
                    beanId.setBean(ref);
                    setSchema(schemaManager.getSchemas(), beanId.getBean());

                }
            }
        }
    }

    @Override
    public Map<String, Schema> getSchemas() {
        Map<String, Schema> schemas = schemaManager.getSchemas();
        return schemas;
    }

    private void setSchema(Map<String, Schema> schemas, Map<BeanId, Bean> beans) {
        for (Bean bean : beans.values()) {
            setSchema(schemas, bean);
        }
    }

    private void setSchema(Map<String, Schema> schemas, Collection<Bean> beans) {
        for (Bean bean : beans) {
            setSchema(schemas, bean);
        }
    }

    private void setSchema(Map<String, Schema> schemas, Bean bean) {
        for (BeanId id : bean.getReferences()) {
            Bean ref = id.getBean();
            if (ref != null) {
                setSchema(schemas, ref);
            }
        }
        Schema s = schemas.get(bean.getId().getSchemaName());
        if (s == null) {
            throw new UnsupportedOperationException(
                    "Schema must always be available for any beans. This is a programming error/bug.");
        }
        bean.set(s);
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
}

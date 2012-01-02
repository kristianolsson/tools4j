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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.AdminContext;
import org.deephacks.tools4j.config.Bean;
import org.deephacks.tools4j.config.Bean.BeanId;
import org.deephacks.tools4j.config.Schema;
import org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.tools4j.config.spi.SchemaManager;
import org.deephacks.tools4j.support.SystemProperties;
import org.deephacks.tools4j.support.lookup.Lookup;

/**
 * AdminCoreContext is responsible for separating the admin and runtime 
 * context so that no dependencies (compile nor runtime) exist between them.
 *   
 */
public class AdminCoreContext extends AdminContext {
    private BeanManager beanManager;
    private SchemaManager schemaManager;

    public AdminCoreContext() {
        beanManager = lookupBeanManager();
        schemaManager = Lookup.get().lookup(SchemaManager.class);

    }

    @Override
    public List<Bean> list(String schemaName) {
        Map<BeanId, Bean> beans = beanManager.list(schemaName);
        Map<String, Schema> schemas = schemaManager.schemaMap();
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
        Map<String, Schema> schemas = schemaManager.schemaMap();
        setSchema(schemas, result);

        return new ArrayList<Bean>(result.values());

    }

    @Override
    public Bean get(BeanId beanId) {
        Bean bean = beanManager.get(beanId);
        bean.set(schemaManager.getSchema(beanId.getSchemaName()));
        setSchema(schemaManager.schemaMap(), bean);
        return bean;
    }

    @Override
    public void create(Bean bean) {
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        bean.set(schema);
        validateSchema(bean);
        beanManager.create(bean);
    }

    @Override
    public void create(Collection<Bean> beans) {
        Map<String, Schema> schemas = schemaManager.schemaMap();
        setSchema(schemas, beans);
        validateSchema(beans);
        beanManager.create(beans);
    }

    @Override
    public void set(Bean bean) {
        SchemaManager schemaManager = Lookup.get().lookup(SchemaManager.class);
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        BeanManager beanManager = Lookup.get().lookup(BeanManager.class);
        bean.set(schema);
        validateSchema(bean);
        beanManager.set(bean);
    }

    @Override
    public void set(Collection<Bean> beans) {
        Map<String, Schema> schemas = schemaManager.schemaMap();
        setSchema(schemas, beans);
        validateSchema(beans);
        beanManager.set(beans);
    }

    @Override
    public void merge(Bean bean) {
        Schema schema = schemaManager.getSchema(bean.getId().getSchemaName());
        bean.set(schema);
        validateSchema(bean);
        beanManager.merge(bean);
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
        beanManager.merge(merges);
    }

    @Override
    public void delete(BeanId beanId) {
        BeanManager beanManager = lookupBeanManager();
        beanManager.delete(beanId);
    }

    @Override
    public void delete(String name, Collection<String> instances) {
        beanManager.delete(name, instances);
    }

    @Override
    public Map<String, Schema> getSchemas() {
        Map<String, Schema> schemas = schemaManager.schemaMap();
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

    public static BeanManager lookupBeanManager() {
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

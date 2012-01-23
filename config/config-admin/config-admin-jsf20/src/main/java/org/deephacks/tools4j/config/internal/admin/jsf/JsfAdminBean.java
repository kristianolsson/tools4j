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
package org.deephacks.tools4j.config.internal.admin.jsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.BasicProperty;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.BasicPropertyList;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.EnumProperty;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.EnumPropertyList;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.IdProperty;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.RefProperty;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.RefPropertyList;
import org.deephacks.tools4j.config.internal.admin.jsf.JsfAdminProperties.RefPropertyMap;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;
import org.deephacks.tools4j.support.event.AbortRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsfAdminBean {
    private Logger log = LoggerFactory.getLogger(JsfAdminBean.class);
    private Bean bean;
    private Schema schema;
    private AdminContext ctx = AdminContext.get();
    private IdProperty id;
    private List<BasicProperty> basicProperties = new ArrayList<BasicProperty>();
    private List<BasicPropertyList> basicListProperties = new ArrayList<BasicPropertyList>();
    private List<EnumProperty> enumProperties = new ArrayList<EnumProperty>();
    private List<EnumPropertyList> enumListProperties = new ArrayList<EnumPropertyList>();
    private List<RefProperty> refProperties = new ArrayList<RefProperty>();
    private List<RefPropertyList> refListProperties = new ArrayList<RefPropertyList>();
    private List<RefPropertyMap> refMapProperties = new ArrayList<RefPropertyMap>();
    private JsfAdminTree currentTree;

    public JsfAdminBean(Bean bean, JsfAdminTree tree) {
        this.bean = bean;

        this.schema = bean.getSchema();
        this.currentTree = tree;
        id = new IdProperty(bean.getId().getInstanceId(), schema.getId().getName(), schema.getId()
                .getDesc());
        for (SchemaProperty p : schema.get(SchemaProperty.class)) {
            if (p.isEnum()) {
                enumProperties.add(new EnumProperty(bean.getSingleValue(p.getName()), p));
            } else {
                basicProperties.add(new BasicProperty(bean.getSingleValue(p.getName()), p));
            }
        }
        for (SchemaPropertyList p : schema.get(SchemaPropertyList.class)) {
            if (p.isEnum()) {
                enumListProperties.add(new EnumPropertyList(bean.getValues(p.getName()), p));
            } else {
                basicListProperties.add(new BasicPropertyList(bean.getValues(p.getName()), p));
            }

        }
        for (SchemaPropertyRef p : schema.get(SchemaPropertyRef.class)) {
            refProperties.add(new RefProperty(bean.getFirstReference(p.getName()), p));
        }
        for (SchemaPropertyRefList p : schema.get(SchemaPropertyRefList.class)) {
            refListProperties.add(new RefPropertyList(bean.getReference(p.getName()), p));
        }
        for (SchemaPropertyRefMap p : schema.get(SchemaPropertyRefMap.class)) {
            refMapProperties.add(new RefPropertyMap(bean.getReference(p.getName()), p));
        }

    }

    public JsfAdminBean(Schema schema, JsfAdminTree tree) {
        this.schema = schema;
        this.currentTree = tree;
        id = new IdProperty(null, schema.getId().getName(), schema.getId().getDesc());
        for (SchemaProperty p : schema.get(SchemaProperty.class)) {
            if (p.isEnum()) {
                enumProperties.add(new EnumProperty(null, p));
            } else {
                basicProperties.add(new BasicProperty(null, p));
            }
        }
        for (SchemaPropertyList p : schema.get(SchemaPropertyList.class)) {
            if (p.isEnum()) {
                enumListProperties.add(new EnumPropertyList(null, p));
            } else {
                basicListProperties.add(new BasicPropertyList(null, p));
            }

        }
        for (SchemaPropertyRef p : schema.get(SchemaPropertyRef.class)) {
            refProperties.add(new RefProperty(null, p));
        }
        for (SchemaPropertyRefList p : schema.get(SchemaPropertyRefList.class)) {
            refListProperties.add(new RefPropertyList(null, p));
        }
        for (SchemaPropertyRefMap p : schema.get(SchemaPropertyRefMap.class)) {
            refMapProperties.add(new RefPropertyMap(null, p));
        }
    }

    public Bean getBean() {
        return bean;
    }

    public Schema getSchema() {
        return schema;
    }

    public List<JsfAdminProperties> getProperties() {
        List<JsfAdminProperties> list = new ArrayList<JsfAdminProperties>();
        list.add(id);
        list.addAll(getBasic());
        list.addAll(getBasicList());
        list.addAll(getEnum());
        list.addAll(getEnumList());
        list.addAll(getRef());
        list.addAll(getRefList());
        list.addAll(getRefMap());
        Collections.sort(list);
        return list;
    }

    public List<BasicProperty> getBasic() {
        return basicProperties;
    }

    public List<BasicPropertyList> getBasicList() {
        return basicListProperties;
    }

    public List<EnumProperty> getEnum() {
        return enumProperties;
    }

    public List<EnumPropertyList> getEnumList() {
        return enumListProperties;
    }

    public List<RefProperty> getRef() {
        return refProperties;
    }

    public List<RefPropertyList> getRefList() {
        return refListProperties;
    }

    public List<RefPropertyMap> getRefMap() {
        return refMapProperties;
    }

    /**
     * This method is used for both creating and editing beans.
     */
    public void onClickSave() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Bean save = null;
            boolean create = false;
            if (bean == null) {
                // this is a create request
                save = Bean.create(BeanId.create(id.getValue().getValue(), schema.getName()));
                create = true;
            } else {
                // this is a edit request
                save = Bean.create(bean.getId());
            }

            for (BasicProperty p : basicProperties) {
                setProperty(save, p);
            }
            for (BasicPropertyList p : basicListProperties) {
                setProperty(save, p);

            }
            for (EnumProperty p : enumProperties) {
                setProperty(save, p);

            }
            for (EnumPropertyList p : enumListProperties) {
                setProperty(save, p);

            }
            for (RefProperty p : refProperties) {
                if (p.getValue().isDirty()) {
                    save.setReference(p.getName(), p.getId());
                }
            }
            for (RefPropertyList p : refListProperties) {
                if (p.getListValues().isDirty()) {
                    save.setReferences(p.getName(), p.getIds());
                }
            }
            for (RefPropertyMap p : refMapProperties) {
                if (p.getListValues().isDirty()) {
                    save.setReferences(p.getName(), p.getIds());
                }
            }
            log.info("onClickSave create={} {} ", create, save);

            if (create) {
                ctx.create(save);
            } else {
                ctx.merge(save);
            }
            context.addMessage(null, new FacesMessage("Operation successful", save.getId()
                    .getInstanceId() + "@" + save.getId().getSchemaName() + " was saved."));
            currentTree.clearCache();
        } catch (AbortRuntimeException e) {
            log.info("", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error occured",
                    e.getEvent().getMessage()));

        } catch (Throwable e) {
            log.info("", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unexpected error occured, see log for details.", e.getMessage()));
        }
    }

    private void setProperty(Bean save, BasicPropertyList p) {
        if (!p.getListValues().isDirty()) {
            return;
        }
        if (p.getListValues().wasResetToDefault()) {
            save.setProperty(p.getName(), p.getListValues().getValues());
            return;
        }
        save.addProperty(p.getName(), p.getListValues().getValues());
    }

    private void setProperty(Bean save, BasicProperty p) {
        if (!p.getValue().isDirty()) {
            return;
        }
        // dirty values.
        if (p.getValue().wasResetToDefault()) {
            save.setProperty(p.getName(), p.getValue().getValue());
            return;
        }
        save.setProperty(p.getName(), p.getValue().getValue());
    }

}

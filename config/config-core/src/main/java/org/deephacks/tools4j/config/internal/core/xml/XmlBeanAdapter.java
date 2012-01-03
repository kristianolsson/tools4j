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
package org.deephacks.tools4j.config.internal.core.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;

public class XmlBeanAdapter {
    @XmlRootElement(name = "bean-xml")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlBeans {
        @XmlElement(name = "bean")
        public List<XmlBean> beans = new ArrayList<XmlBean>();

        public XmlBeans() {

        }

        public XmlBeans(List<Bean> beans) {
            for (Bean bean : beans) {
                this.beans.add(new XmlBean(bean));
            }
        }

        public List<Bean> getBeans() {
            ArrayList<Bean> result = new ArrayList<Bean>();
            for (XmlBean b : beans) {
                Bean bean = Bean.create(BeanId.create(b.id, b.name));
                for (XmlBeanPropertyList p : b.properties) {
                    if (p.values != null)
                        bean.addProperty(p.name, p.values);
                }
                for (XmlBeanPropertyRefList p : b.references) {
                    if (p.refs != null)
                        bean.addReference(p.name, p.getReferences());
                }

                result.add(bean);
            }
            return result;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        static public class XmlBean {
            @XmlAttribute
            public String id;
            @XmlAttribute
            public String name;
            @XmlElement(name = "prop")
            public List<XmlBeanPropertyList> properties = new ArrayList<XmlBeanPropertyList>();
            @XmlElement(name = "ref")
            public List<XmlBeanPropertyRefList> references = new ArrayList<XmlBeanPropertyRefList>();

            public XmlBean() {

            }

            public XmlBean(Bean bean) {
                this.id = bean.getId().getInstanceId();
                this.name = bean.getId().getSchemaName();
                for (String name : bean.getPropertyNames()) {
                    List<String> values = bean.getValues(name);
                    if (values == null || values.size() == 0) {
                        continue;
                    }
                    properties.add(new XmlBeanPropertyList(name, values));
                }
                for (String name : bean.getReferenceNames()) {
                    List<BeanId> ids = bean.getReference(name);
                    if (ids == null || ids.size() == 0) {
                        continue;
                    }
                    String schemaName = ids.get(0).getSchemaName();
                    references.add(new XmlBeanPropertyRefList(name, schemaName, ids));

                }

            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlBeanPropertyList {
            @XmlAttribute
            private String name;
            @XmlElement(name = "val")
            private List<String> values = new ArrayList<String>();

            public XmlBeanPropertyList() {
            }

            public XmlBeanPropertyList(String name, List<String> values) {
                this.name = name;
                if (values == null) {
                    return;
                }
                this.values = values;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlBeanPropertyRefList {
            @XmlAttribute
            private String name;
            @XmlAttribute(name = "schema")
            private String schema;
            @XmlElement(name = "id")
            private List<String> refs;

            public XmlBeanPropertyRefList() {
            }

            public XmlBeanPropertyRefList(String name, String schema, List<BeanId> values) {
                this.name = name;
                this.refs = new ArrayList<String>();
                for (BeanId beanId : values) {
                    this.refs.add(beanId.getInstanceId());
                }
                this.schema = schema;
            }

            public List<BeanId> getReferences() {
                ArrayList<BeanId> ids = new ArrayList<BeanId>();
                for (String id : refs) {
                    ids.add(BeanId.create(id, schema));
                }
                return ids;

            }
        }

    }
}

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

import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaId;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;

public class XmlSchemaAdapter {
    @XmlRootElement(name = "schema-xml")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlSchemas {
        @XmlElement(name = "schema")
        public List<XmlSchema> schemas = new ArrayList<XmlSchema>();

        public XmlSchemas() {

        }

        public XmlSchemas(List<Schema> schemas) {
            for (Schema bean : schemas) {
                this.schemas.add(new XmlSchema(bean));
            }
        }

        public List<Schema> getSchemas() {
            ArrayList<Schema> result = new ArrayList<Schema>();
            for (XmlSchema b : schemas) {
                Schema schema = Schema.create(
                        SchemaId.create(b.id.name, b.id.desc, b.id.singleton), b.type, b.name,
                        b.desc);
                for (XmlSchemaProperty p : b.properties) {
                    schema.add(SchemaProperty.create(p.name, p.fieldName, p.type, p.desc,
                            p.isImmutable, p.defaultValue));
                }
                for (XmlSchemaCollection p : b.collection) {
                    schema.add(SchemaPropertyList.create(p.name, p.fieldName, p.parameterizedType,
                            p.desc, p.isImmutable, p.defaultValues, p.collectionType));
                }
                for (XmlSchemaRef p : b.ref) {
                    schema.add(SchemaPropertyRef.create(p.name, p.fieldName, p.schemaName, p.desc,
                            p.isImmutable, p.isSingleton));
                }
                for (XmlSchemaRefCollection p : b.refCollection) {
                    schema.add(SchemaPropertyRefList.create(p.name, p.fieldName, p.schemaName,
                            p.desc, p.isImmutable, p.collectionType));
                }
                result.add(schema);
            }
            return result;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        static public class XmlSchema {
            @XmlAttribute
            private String name;
            @XmlAttribute
            private String type;
            @XmlAttribute
            private String desc;

            private XmlSchemaId id;

            @XmlElement(name = "property")
            public List<XmlSchemaProperty> properties = new ArrayList<XmlSchemaProperty>();
            @XmlElement(name = "collection")
            public List<XmlSchemaCollection> collection = new ArrayList<XmlSchemaCollection>();
            @XmlElement(name = "ref")
            public List<XmlSchemaRef> ref = new ArrayList<XmlSchemaRef>();
            @XmlElement(name = "ref-collection")
            public List<XmlSchemaRefCollection> refCollection = new ArrayList<XmlSchemaRefCollection>();

            public XmlSchema() {

            }

            public XmlSchema(Schema bean) {
                this.id = new XmlSchemaId(bean.getId());
                this.name = bean.getName();
                this.type = bean.getType();
                this.desc = bean.getDesc();

                for (SchemaProperty p : bean.get(SchemaProperty.class)) {
                    properties.add(new XmlSchemaProperty(p));
                }
                for (SchemaPropertyList p : bean.get(SchemaPropertyList.class)) {
                    collection.add(new XmlSchemaCollection(p));
                }
                for (SchemaPropertyRef p : bean.get(SchemaPropertyRef.class)) {
                    ref.add(new XmlSchemaRef(p));
                }
                for (SchemaPropertyRefList p : bean.get(SchemaPropertyRefList.class)) {
                    refCollection.add(new XmlSchemaRefCollection(p));
                }

            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlSchemaId {
            @XmlAttribute
            private String name;
            @XmlAttribute
            private String desc;
            @XmlAttribute
            private boolean singleton;

            public XmlSchemaId() {

            }

            public XmlSchemaId(SchemaId id) {
                this.name = id.getName();
                this.desc = id.getDesc();
                this.singleton = id.isSingleton();
            }

        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlSchemaProperty {
            @XmlAttribute
            private String name;
            @XmlAttribute(name = "field-name")
            private String fieldName;
            @XmlAttribute
            private String type;
            @XmlAttribute
            private String desc;
            @XmlAttribute(name = "immutable")
            private boolean isImmutable;
            @XmlElement(name = "default")
            private String defaultValue;

            public XmlSchemaProperty() {
                // required by JAXB.
            }

            public XmlSchemaProperty(SchemaProperty p) {
                this.name = p.getName();
                this.fieldName = p.getFieldName();
                this.type = p.getType();
                this.defaultValue = p.getDefaultValue();
                this.desc = p.getDesc();
                this.isImmutable = p.isImmutable();
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlSchemaCollection {
            @XmlAttribute
            private String name;
            @XmlAttribute(name = "field-name")
            private String fieldName;
            @XmlAttribute(name = "parameterized-type")
            private String parameterizedType;
            @XmlAttribute(name = "immutable")
            private boolean isImmutable;
            @XmlAttribute(name = "collection-type")
            public String collectionType;
            @XmlAttribute
            private String desc;
            @XmlElement(name = "default")
            private List<String> defaultValues = new ArrayList<String>();

            public XmlSchemaCollection() {
            }

            public XmlSchemaCollection(SchemaPropertyList p) {
                this.name = p.getName();
                this.fieldName = p.getFieldName();
                this.parameterizedType = p.getType();
                this.collectionType = p.getCollectionType();
                this.desc = p.getDesc();
                this.isImmutable = p.isImmutable();
                this.defaultValues = p.getDefaultValues();
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlSchemaRef {
            @XmlAttribute
            private String name;
            @XmlAttribute(name = "field-name")
            private String fieldName;
            @XmlAttribute(name = "schema-name")
            private String schemaName;
            @XmlAttribute(name = "immutable")
            private boolean isImmutable;
            @XmlAttribute(name = "singleton")
            private boolean isSingleton;
            @XmlAttribute
            private String desc;

            public XmlSchemaRef() {
                // required by JAXB.
            }

            public XmlSchemaRef(SchemaPropertyRef p) {
                this.name = p.getName();
                this.fieldName = p.getFieldName();
                this.schemaName = p.getSchemaName();
                this.desc = p.getDesc();
                this.isImmutable = p.isImmutable();
                this.isSingleton = p.isSingleton();
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class XmlSchemaRefCollection {
            @XmlAttribute
            private String name;
            @XmlAttribute(name = "field-name")
            private String fieldName;
            @XmlAttribute(name = "schema-name")
            private String schemaName;
            @XmlAttribute(name = "collection-type")
            private String collectionType;
            @XmlAttribute(name = "immutable")
            private boolean isImmutable;
            @XmlAttribute
            private String desc;

            public XmlSchemaRefCollection() {
            }

            public XmlSchemaRefCollection(SchemaPropertyRefList p) {
                this.name = p.getName();
                this.fieldName = p.getFieldName();
                this.schemaName = p.getSchemaName();
                this.collectionType = p.getCollectionType();
                this.desc = p.getDesc();
                this.isImmutable = p.isImmutable();
            }
        }

    }

}

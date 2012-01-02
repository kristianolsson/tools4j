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
package org.deephacks.tools4j.config;

import static com.google.common.base.Objects.equal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * <p>
 * Schema is also used to provide information to administrative users in order to explain 
 * its purpose and how configuration changes may affect system behaviour.
 * </p>
 * <p>
 * Schemas are handled by the framework and are not intended for spontaneous creation or
 * modification by any user. Schemas originate from when a configurable class is registered 
 * in the system and are considered read-only entities.
 * </p>
 * 
 * @author Kristoffer Sjogren
 */
public class Schema implements Serializable {
    private static final long serialVersionUID = -2914939410489202548L;
    private SchemaId id;
    private String name;
    private String type;
    private String description;
    private String multiplicity;
    private Multimap<Class<? extends AbstractSchemaProperty>, AbstractSchemaProperty> properties = ArrayListMultimap
            .create();

    private Schema(SchemaId id, String type, String name, String description, String multiplicity) {
        this.id = Preconditions.checkNotNull(id);
        this.type = Preconditions.checkNotNull(type);
        this.description = Preconditions.checkNotNull(description);
        this.name = Preconditions.checkNotNull(name);
        this.multiplicity = Preconditions.checkNotNull(multiplicity);
    }

    /**
     * Creates a new schema. Not to be used by users, schemas are created when a configurable class are
     * registered in the system.
     * 
     * @param id that identify this schema. 
     * @param classType classname that fully qualifies the configurable class that this schema originates from.
     * @param name of this schema as specified in meta data, names must be unique.
     * @param description purpose and useful information needed in order to manage this schema.
     * @param multiplicity indicates how many instances that may exist of this schema. 
     * @return A Schema.
     */
    public static Schema create(SchemaId id, String classType, String name, String description,
            String multiplicity) {
        return new Schema(id, classType, name, description, multiplicity);
    }

    /**
     * Identification for this this schema. This id must be unqiue in the system.
     *  
     * @return id for this schema.
     */
    public SchemaId getId() {
        return id;
    }

    public String getMultiplicity() {
        return multiplicity;
    }

    /**
     * This is the fully qualified classname of the configurable class that this schema originates from.
     * 
     * Do not display this property to end users, use the 'name' property instead . 
     * 
     * @return A full class name.  
     */
    public String getType() {
        return type;
    }

    /**
     * Description that justify the existence of the schema by putting it into a
     * high-level context and describe how it relates to system concepts.
     * 
     * @return description
     */
    public String getDesc() {
        return description;
    }

    /**
     * A unique name that identifies the schema in the system. Good names are those
     * which describe domain specific aspects established in the system architecture.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a property of a specific type to this schema. Not to be used by users. 
     * 
     * @param property
     */
    public void add(AbstractSchemaProperty property) {
        properties.put(property.getClass(), property);
    }

    /**
     * Returns all the properties of a particular type. 
     * 
     * @param clazz The specific type of properties to get.
     * @return A list of properties that matches the clazz.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractSchemaProperty> Collection<T> get(Class<T> clazz) {
        return (Collection<T>) properties.get(clazz);
    }

    /**
     * Returns a specific properties of a particular type identified with a name.
     * 
     * @param clazz specific type of property to get.
     * @param name The AbstractSchemaProperty name of the property.
     * @return Matching property.
     */
    public <T extends AbstractSchemaProperty> T get(Class<T> clazz, String name) {
        Collection<T> propertyCollection = get(clazz);
        for (T property : propertyCollection) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(Schema.class).add("id", id).add("name", getName())
                .add("type", getType()).add("desc", getDesc())
                .add("multiplicity", getMultiplicity()).add("properties", properties).toString();

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties, getType(), getDesc(), getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Schema)) {
            return false;
        }
        Schema o = (Schema) obj;
        return equal(getName(), o.getName()) && equal(getType(), o.getType());
    }

    /**
     * Description of the identification of a a particular schema registered 
     * in the system.
     */
    public static class SchemaId implements Serializable {
        private static final long serialVersionUID = 5803256931889425514L;
        private String name;
        private String desc;

        private SchemaId(String name, String desc) {
            this.name = Preconditions.checkNotNull(name);
            this.desc = Preconditions.checkNotNull(desc);
        }

        public static SchemaId create(String name, String desc) {
            return new SchemaId(name, desc);
        }

        /**
         * 
         * @return
         */
        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String toString() {
            return Objects.toStringHelper(SchemaId.class).add("id", getName())
                    .add("desc", getDesc()).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName(), getDesc());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaId)) {
                return false;
            }
            SchemaId o = (SchemaId) obj;
            return equal(getName(), o.getName()) && equal(getDesc(), o.getDesc());
        }
    }

    /**
     * Abstract base class for schema properties.
     */
    public static class AbstractSchemaProperty implements Serializable {
        private static final long serialVersionUID = -3057627766413883885L;
        private String name;
        private String desc;
        private String fieldName;

        protected AbstractSchemaProperty(String name, String fieldName, String desc) {
            this.name = Preconditions.checkNotNull(name);
            this.fieldName = Preconditions.checkNotNull(fieldName);
            this.desc = Preconditions.checkNotNull(desc);
        }

        public String getName() {
            return this.name;
        }

        public String getFieldName() {
            return this.fieldName;
        }

        public String getDesc() {
            return this.desc;
        }

        ToStringHelper toStringHelper(Class<?> clazz) {
            return Objects.toStringHelper(clazz).add("name", name).add("fieldName", fieldName)
                    .add("desc", desc);
        }

        int getHashCode() {
            return Objects.hashCode(getName(), getFieldName(), getDesc());
        }

        boolean equals(AbstractSchemaProperty o) {
            return equal(getName(), o.getName()) && equal(getFieldName(), o.getFieldName())
                    && equal(getDesc(), o.getDesc());
        }
    }

    /**
     * Description of a single simple type.
     */
    public static class SchemaProperty extends AbstractSchemaProperty {
        private static final long serialVersionUID = -8108590860088240249L;
        private String defaultValue;
        private String type;

        private SchemaProperty(String name, String fieldName, String type, String desc,
                String defaultValue) {
            super(name, fieldName, desc);
            this.defaultValue = defaultValue;
            this.type = Preconditions.checkNotNull(type);
        }

        /**
         * Not to be used by users.
         * 
         * @param name
         * @param fieldName
         * @param type
         * @param desc
         * @param defaultValue
         * @return
         */
        public static SchemaProperty create(String name, String fieldName, String type,
                String desc, String defaultValue) {
            return new SchemaProperty(name, fieldName, type, desc, defaultValue);
        }

        public String getType() {
            return type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String toString() {
            return super.toStringHelper(SchemaProperty.class).add("type", getType())
                    .add("defaultValue", getDefaultValue()).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.getHashCode(), getDefaultValue(), getType());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaProperty)) {
                return false;
            }
            SchemaProperty o = (SchemaProperty) obj;
            return super.equals(o) && equal(getDefaultValue(), o.getDefaultValue())
                    && equal(getType(), o.getType());
        }
    }

    /**
     * Description of a collection (or any other subtype) of simple types.
     */
    public static class SchemaPropertyList extends AbstractSchemaProperty {
        private static final long serialVersionUID = 3192273741446945936L;
        private String type;
        private String collectionType;
        private List<String> defaultValues;

        private SchemaPropertyList(String name, String fieldName, String type, String desc,
                String collectionType, List<String> defaultValues) {
            super(name, fieldName, desc);
            this.collectionType = Preconditions.checkNotNull(collectionType);
            this.type = Preconditions.checkNotNull(type);
            this.defaultValues = defaultValues;
        }

        /**
         * Not to be used by users.
         * 
         * @param name
         * @param fieldName
         * @param type
         * @param desc
         * @param defaultValues
         * @param collectionType
         * @return
         */
        public static SchemaPropertyList create(String name, String fieldName, String type,
                String desc, List<String> defaultValues, String collectionType) {
            return new SchemaPropertyList(name, fieldName, type, desc, collectionType,
                    defaultValues);
        }

        public String getType() {
            return type;
        }

        public List<String> getDefaultValues() {
            return defaultValues;
        }

        public String getCollectionType() {
            return collectionType;
        }

        public String toString() {
            return Objects.toStringHelper(SchemaPropertyList.class)
                    .add("type", getCollectionType()).add("collectionType", getCollectionType())
                    .add("defaultValue", getDefaultValues()).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.getHashCode(), getType(), getCollectionType(),
                    getDefaultValues());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaPropertyList)) {
                return false;
            }
            SchemaPropertyList o = (SchemaPropertyList) obj;
            return equals(o) && equal(getCollectionType(), o.getCollectionType())
                    && equal(getDefaultValues(), o.getDefaultValues())
                    && equal(getType(), o.getType());
        }
    }

    /**
     * Description of a single reference to any other bean registered in the system.
     */
    public static class SchemaPropertyRef extends AbstractSchemaProperty {
        private String schemaName;
        private static final long serialVersionUID = 987642987178370676L;

        protected SchemaPropertyRef(String name, String fieldName, String schemaName, String desc) {
            super(name, fieldName, desc);
            this.schemaName = Preconditions.checkNotNull(schemaName);
        }

        /**
         * Not to be used by users.
         * 
         * @param name
         * @param fieldName
         * @param schemaName
         * @param desc
         * @return
         */
        public static SchemaPropertyRef create(String name, String fieldName, String schemaName,
                String desc) {
            return new SchemaPropertyRef(name, fieldName, schemaName, desc);
        }

        public String getSchemaName() {
            return schemaName;
        }

        @Override
        public int hashCode() {
            return super.getHashCode() + Objects.hashCode(getSchemaName());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaPropertyRef)) {
                return false;
            }
            SchemaPropertyRef o = (SchemaPropertyRef) obj;
            return equals(o) && equal(getSchemaName(), o.getSchemaName());
        }

        @Override
        public String toString() {
            return super.toStringHelper(SchemaPropertyRef.class).add("schema-name", schemaName)
                    .toString();
        }
    }

    /**
     * Description of a collection (or any other subtype) of references to any other 
     * bean defined in the system.
     */
    public static class SchemaPropertyRefList extends AbstractSchemaProperty {

        private static final long serialVersionUID = -2386455434996679436L;
        private String collectionType;
        private String schemaName;

        private SchemaPropertyRefList(String name, String fieldName, String schemaName,
                String desc, String collectionType) {
            super(name, fieldName, desc);
            this.collectionType = Preconditions.checkNotNull(collectionType);
            this.schemaName = Preconditions.checkNotNull(schemaName);
        }

        /**
         * Not to be used by users.
         * 
         * @param name
         * @param fieldName
         * @param schemaName
         * @param desc
         * @param collectionType
         * @return
         */
        public static SchemaPropertyRefList create(String name, String fieldName,
                String schemaName, String desc, String collectionType) {
            return new SchemaPropertyRefList(name, fieldName, schemaName, desc, collectionType);
        }

        public String getCollectionType() {
            return collectionType;
        }

        public String getSchemaName() {
            return schemaName;
        }

        @Override
        public int hashCode() {
            return super.getHashCode() + Objects.hashCode(collectionType, schemaName);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaPropertyRefList)) {
                return false;
            }
            SchemaPropertyRefList o = (SchemaPropertyRefList) obj;
            return equals(o) && equal(getCollectionType(), o.getCollectionType())
                    && equal(getSchemaName(), o.getSchemaName());
        }

        @Override
        public String toString() {
            return toStringHelper(SchemaPropertyRefList.class)
                    .add("collectionType", getCollectionType()).add("schema-name", getSchemaName())
                    .toString();
        }
    }
}

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Schema.SchemaProperty;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;

public abstract class JsfAdminProperties implements Comparable<JsfAdminProperties> {
    public String getSimpleClassname() {
        return this.getClass().getSimpleName();
    }

    public int compareTo(JsfAdminProperties o) {
        return this.toString().compareTo(o.toString());
    }

    public static class IdProperty extends JsfAdminProperties {
        private Value value;
        private String type;
        private String name;
        private String desc;

        public IdProperty(String value, String name, String desc) {
            this.value = new Value(value, false, null);
            this.name = name;
            this.desc = desc;
            this.type = String.class.getName();
        }

        public Value getValue() {
            return value;
        }

        public boolean getIsImmutable() {
            return value.isImmutable;
        }

        public String getTypeDisplay() {
            return getSimpleTypeDisplay(getType());
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    public static class BasicProperty extends JsfAdminProperties {
        private Value value;
        private String type;
        private String name;
        private String desc;

        public BasicProperty(String value, SchemaProperty prop) {
            this.value = new Value(value, prop.isImmutable(), prop.getDefaultValue());
            this.name = prop.getName();
            this.desc = prop.getDesc();
            this.type = prop.getType();
        }

        public Value getValue() {
            return value;
        }

        public boolean getIsImmutable() {
            return value.isImmutable;
        }

        public String getTypeDisplay() {
            return getSimpleTypeDisplay(getType());
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public void resetToDefault() {
            value.resetToDefault();
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static class BasicPropertyList extends JsfAdminProperties {
        private ListValues listValues;
        private List<String> defaultValue = new ArrayList<String>();
        private String type;
        private String name;
        private String desc;

        public BasicPropertyList(List<String> values, SchemaPropertyList prop) {
            this.listValues = new ListValues(values, prop.isImmutable(), prop.getDefaultValues());
            this.defaultValue = prop.getDefaultValues();
            this.name = prop.getName();
            this.desc = prop.getDesc();
            this.type = prop.getType();
        }

        public ListValues getListValues() {
            return listValues;
        }

        public boolean getIsImmutable() {
            return listValues.isImmutable;
        }

        public void setListValues(ListValues listValues) {
            this.listValues = listValues;
        }

        public List<String> getDefaultValues() {
            return defaultValue;
        }

        public String getTypeDisplay() {
            return "List of " + getSimpleTypeDisplay(getType());
        }

        public String getType() {
            return type;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    public static class EnumProperty extends BasicProperty {
        private List<String> possibleValues = new ArrayList<String>();

        public EnumProperty(String value, SchemaProperty prop) {
            super(value, prop);
            try {
                Class<?> clazz = Class.forName(getType());
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isEnumConstant()) {
                        Object aEnum = f.get(null);
                        possibleValues.add(aEnum.toString());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<String> getPossibleValues() {
            return possibleValues;
        }
    }

    public static class EnumPropertyList extends BasicPropertyList {
        private List<String> possibleValues = new ArrayList<String>();

        public EnumPropertyList(List<String> values, SchemaPropertyList prop) {
            super(values, prop);
            try {
                Class<?> clazz = Class.forName(getType());
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isEnumConstant()) {
                        Object aEnum = f.get(null);
                        possibleValues.add(aEnum.toString());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public List<String> getPossibleValues() {
            return possibleValues;
        }
    }

    public static class RefProperty extends JsfAdminProperties {
        private Value value;
        private String type;
        private String name;
        private String desc;

        public RefProperty(BeanId id, SchemaPropertyRef ref) {
            this.name = ref.getName();
            this.type = ref.getSchemaName();
            this.desc = ref.getDesc();
            if (id == null) {
                this.value = new Value(null, ref.isImmutable(), null);
            } else {
                this.value = new Value(id.getInstanceId(), ref.isImmutable(), null);
            }

        }

        public BeanId getId() {
            if (value.getValue() == null || "".equals(value.getValue())) {
                return null;
            }
            return BeanId.create(value.getValue(), type);
        }

        public Value getValue() {
            return value;
        }

        public boolean getIsImmutable() {
            return value.isImmutable;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public String getTypeDisplay() {
            return getSimpleTypeDisplay(getType());
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return getName();
        }

    }

    public static class RefPropertyList extends JsfAdminProperties {
        protected ListValues listValues;
        private String type;
        private String name;
        private String desc;

        public RefPropertyList(List<BeanId> ids, SchemaPropertyRefList prop) {
            this.name = prop.getName();
            this.desc = prop.getDesc();
            this.type = prop.getSchemaName();
            ArrayList<String> instanceIds = new ArrayList<String>();
            if (ids != null) {
                for (BeanId beanId : ids) {
                    instanceIds.add(beanId.getInstanceId());
                }
            }
            this.listValues = new ListValues(instanceIds, prop.isImmutable(), null);
        }

        public List<BeanId> getIds() {
            List<BeanId> ids = new ArrayList<BeanId>();
            for (String value : listValues.getValues()) {
                ids.add(BeanId.create(value, type));
            }
            return ids;
        }

        public ListValues getListValues() {
            return listValues;
        }

        public boolean getIsImmutable() {
            return listValues.isImmutable;
        }

        public String getTypeDisplay() {
            return "List of " + getSimpleTypeDisplay(getType());
        }

        public void setListCreator(ListValues listValues) {
            this.listValues = listValues;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public void resetToDefault() {
            listValues.resetToDefault();
        }

        public boolean getIsDefault() {
            return listValues.getValues() == null;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static class RefPropertyMap extends JsfAdminProperties {
        protected ListValues listValues;
        private String type;
        private String name;
        private String desc;

        public RefPropertyMap(List<BeanId> ids, SchemaPropertyRefMap prop) {
            this.name = prop.getName();
            this.desc = prop.getDesc();
            this.type = prop.getSchemaName();
            ArrayList<String> instanceIds = new ArrayList<String>();
            if (ids != null) {
                for (BeanId beanId : ids) {
                    if (!instanceIds.contains(beanId.getInstanceId())) {
                        instanceIds.add(beanId.getInstanceId());
                    }
                }
            }
            this.listValues = new ListValues(instanceIds, prop.isImmutable(), null);
        }

        public List<BeanId> getIds() {
            List<BeanId> ids = new ArrayList<BeanId>();
            for (String value : listValues.getValues()) {
                ids.add(BeanId.create(value, type));
            }
            return ids;
        }

        public ListValues getListValues() {
            return listValues;
        }

        public boolean getIsImmutable() {
            return listValues.isImmutable;
        }

        public String getTypeDisplay() {
            return "Map of " + getSimpleTypeDisplay(getType());
        }

        public void setListCreator(ListValues listValues) {
            this.listValues = listValues;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public void resetToDefault() {
            listValues.resetToDefault();
        }

        public boolean getIsDefault() {
            return listValues.getValues() == null;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static class Value {
        private String value;
        private String schemaDefaultValue;
        private boolean isImmutable;
        private boolean isDirty;
        private boolean resetToDefault;
        private boolean isDefaultValue;

        public Value(String value, boolean isImmutable, String schemaDefaultValue) {
            this.schemaDefaultValue = schemaDefaultValue;
            this.value = value;
            this.isImmutable = isImmutable;
            // if no default value exist, display "null" instead.
            if (schemaDefaultValue == null || "".equals(schemaDefaultValue)) {
                this.schemaDefaultValue = "null";
            }
            // empty value, use default
            if (value == null || "".equals(value)) {
                isDefaultValue = true;
            }
        }

        public String getValue() {
            return value;
        }

        public boolean getIsImmutable() {
            return isImmutable;
        }

        public void setValue(String value) {
            if (value == null || "".equals(value)) {
                return;
            }
            if (this.value == null || !this.value.equals(value)) {
                this.isDirty = true;
                this.isDefaultValue = false;
                this.resetToDefault = false;
            }
            this.value = value;
        }

        public String getSchemaDefaultValue() {
            return schemaDefaultValue;
        }

        public boolean getIsDefaultValue() {
            return isDefaultValue;
        }

        public boolean getIsEmpty() {
            return value == null || "".equals(value);
        }

        public boolean isDirty() {
            return isDirty;
        }

        public boolean wasResetToDefault() {
            return resetToDefault;
        }

        public void resetToDefault() {
            if (!isDefaultValue) {
                isDirty = true;
            }
            value = null;
            resetToDefault = true;
            isDefaultValue = true;
        }
    }

    public static class ListValues {
        private List<String> values = new ArrayList<String>();
        private List<String> schemaDefaultValues = new ArrayList<String>();
        private boolean isImmutable;
        private String inputTextValue;
        private String menuItemValue;
        private boolean isDirty = false;
        private boolean resetToDefault;
        private boolean isDefaultValue;

        public ListValues(List<String> values, boolean isImmutable, List<String> schemaDefaultValues) {
            this.values = values;
            this.isImmutable = isImmutable;
            this.schemaDefaultValues = schemaDefaultValues;
            // if no default value exist, display "null" instead.
            if (schemaDefaultValues == null || "".equals(schemaDefaultValues)) {
                this.schemaDefaultValues = new ArrayList<String>();
            }
            // empty value, use default
            if (values == null || "".equals(values)) {
                isDefaultValue = true;
            }
            this.values = values;

        }

        public List<String> getValues() {
            return values;
        }

        public boolean getIsImmutable() {
            return isImmutable;
        }

        public void setValues(List<String> values) {
            // empty value, use default
            if (values == null) {
                isDefaultValue = true;
                this.resetToDefault = true;
            }
            if (this.values != null && values == null) {
                this.isDirty = true;
                this.isDefaultValue = true;
                this.resetToDefault = true;
            }
            if (this.values == null && values != null) {
                this.isDirty = true;
                this.isDefaultValue = false;
                this.resetToDefault = false;
            }

            this.values = values;
        }

        public void setInputTextValue(String value) {
            this.inputTextValue = value;
        }

        public String getInputTextValue() {
            return this.inputTextValue;
        }

        public void setMenuItemValue(String value) {
            this.menuItemValue = value;
        }

        public String getMenuItemValue() {
            return this.menuItemValue;
        }

        public void addValue() {
            if (values == null) {
                values = new ArrayList<String>();
            }
            values.add(inputTextValue);
            isDirty = true;
            isDefaultValue = false;
            resetToDefault = false;
        }

        public void deleteValue() {
            if (values == null) {
                return;
            }
            if (values.contains(menuItemValue)) {
                isDirty = true;
                isDefaultValue = false;
                resetToDefault = false;
                values.remove(menuItemValue);
            }
        }

        public boolean isDirty() {
            return isDirty;
        }

        public List<String> getSchemaDefaultValues() {
            return schemaDefaultValues;
        }

        public boolean wasResetToDefault() {
            return resetToDefault;
        }

        public void resetToDefault() {
            resetToDefault = true;
            setValues(null);
        }

        public boolean getIsDefaultValue() {
            return isDefaultValue;
        }
    }

    static final String getSimpleTypeDisplay(String type) {
        return type.substring(type.lastIndexOf(".") + 1, type.length());

    }
}

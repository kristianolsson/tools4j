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
package org.deephacks.tools4j.config.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.support.types.DateTime;
import org.deephacks.tools4j.support.types.DurationTime;

public class ConfigTestData {

    public ConfigTestData() {

    }

    public Grandfather getGrandfather(String id) {
        try {
            Grandfather gf = new Grandfather(id);
            gf.id = id;
            gf.prop1 = "value";
            gf.prop2 = new HashSet<String>(Arrays.asList("c", "b", "a"));
            gf.prop4 = new DateTime("2002-09-24-06:00");
            gf.prop5 = new DurationTime("PT15H");
            gf.prop8 = new Byte((byte) 1);
            gf.prop9 = new Long(1000000000000L);
            gf.prop10 = new Short((short) 123);
            gf.prop11 = new Float(12313.13);
            gf.prop12 = new Double(238.476238746834796);
            gf.prop13 = new Boolean(true);
            gf.prop14 = TimeUnit.NANOSECONDS;
            gf.prop15 = new URL("http://www.deephacks.org");
            gf.prop16 = new File(".").getAbsoluteFile();
            gf.prop17 = Arrays.asList(new File(".").getAbsoluteFile(),
                    new File(".").getAbsoluteFile());
            gf.prop18 = Arrays.asList(new URL("http://www.deephacks.org"), new URL(
                    "http://www.google.se"));
            gf.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
            return gf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Grandfather getEmptyGrandfather(String id) {
        Grandfather empty = new Grandfather(id);
        return empty;
    }

    public Parent getParent(String parentId) {
        try {
            Parent gf = new Parent(parentId);
            gf.id = parentId;
            gf.prop1 = "value";
            gf.prop2 = new HashSet<String>(Arrays.asList("c", "b", "a"));
            gf.prop4 = new DateTime("2002-09-24-06:00");
            gf.prop5 = new DurationTime("PT15H");
            gf.prop8 = new Byte((byte) 1);
            gf.prop9 = new Long(1000000000000L);
            gf.prop10 = new Short((short) 123);
            gf.prop11 = new Float(12313.13);
            gf.prop12 = new Double(238.476238746834796);
            gf.prop13 = new Boolean(true);
            gf.prop15 = new URL("http://www.deephacks.org");
            gf.prop16 = new File(".").getAbsoluteFile();
            gf.prop17 = Arrays.asList(new File(".").getAbsoluteFile(),
                    new File(".").getAbsoluteFile());
            gf.prop18 = Arrays.asList(new URL("http://www.deephacks.org"), new URL(
                    "http://www.google.se"));
            gf.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
            return gf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Child getChild(String childId) {
        try {
            Child gf = new Child(childId);
            gf.id = childId;
            gf.prop2 = new HashSet<String>(Arrays.asList("c", "b", "a"));
            gf.prop4 = new DateTime("2002-09-24-06:00");
            gf.prop5 = new DurationTime("PT15H");
            gf.prop8 = new Byte((byte) 1);
            gf.prop9 = new Long(1000000000000L);
            gf.prop10 = new Short((short) 123);
            gf.prop11 = new Float(12313.13);
            gf.prop12 = new Double(238.476238746834796);
            gf.prop13 = new Boolean(true);
            gf.prop15 = new URL("http://www.deephacks.org");
            gf.prop16 = new File(".").getAbsoluteFile();
            gf.prop17 = Arrays.asList(new File(".").getAbsoluteFile(),
                    new File(".").getAbsoluteFile());
            gf.prop18 = Arrays.asList(new URL("http://www.deephacks.org"), new URL(
                    "http://www.google.se"));
            gf.prop19 = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
            return gf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SingletonParent getSingletonParent(String id) {
        return new SingletonParent(id);
    }

    public Singleton getSingleton() {
        return new Singleton();
    }

    public static final String GRANDFATHER_SCHEMA_NAME = "GrandfatherSchemaName";

    @Config(name = GRANDFATHER_SCHEMA_NAME, desc = "a test class")
    public class Grandfather {

        @Id(name = "id", desc = "desc")
        public String id;
        @Config(desc = "prop1Desc")
        public String prop1 = "defaultValue";
        @Config(desc = "prop2Desc")
        public Set<String> prop2;
        @Config(desc = "prop3Desc")
        public List<Integer> prop3 = Arrays.asList(1, 2, 3);
        @Config(desc = "prop4Desc")
        public DateTime prop4;
        @Config(desc = "prop5Desc")
        public DurationTime prop5;
        @Config(desc = "prop7Desc")
        public List<Parent> prop7;
        @Config(desc = "prop8Desc")
        public Byte prop8;
        @Config(desc = "prop9Desc")
        public Long prop9;
        @Config(desc = "prop10Desc")
        public Short prop10;
        @Config(desc = "prop11Desc")
        public Float prop11;
        @Config(desc = "prop12Desc")
        public Double prop12;
        @Config(desc = "prop13Desc")
        public Boolean prop13;
        @Config(desc = "prop14Desc")
        public TimeUnit prop14 = TimeUnit.MICROSECONDS;
        @Config(desc = "prop15Desc")
        public URL prop15;
        @Config(desc = "prop16Desc")
        public File prop16;
        @Config(desc = "prop17Desc")
        public List<File> prop17;
        @Config(desc = "prop18Desc")
        public List<URL> prop18;
        @Config(desc = "prop19Desc")
        public List<TimeUnit> prop19 = Arrays.asList(TimeUnit.HOURS, TimeUnit.SECONDS);

        @Config(desc = "prop20Desc")
        public Map<String, Parent> prop20;

        public void add(Parent... p) {
            if (prop7 == null) {
                prop7 = new ArrayList<Parent>();
            }
            prop7.addAll(Arrays.asList(p));
        }

        public List<Parent> getParents() {
            return prop7;
        }

        public void resetParents() {
            prop7.clear();
        }

        public void put(Parent p) {
            if (prop20 == null) {
                prop20 = new HashMap<String, Parent>();
            }
            prop20.put(p.id, p);
        }

        public Grandfather() {
        }

        public BeanId getId() {
            return BeanId.create(id, GRANDFATHER_SCHEMA_NAME);
        }

        public Grandfather(String id) {
            this.id = id;
        }

    }

    public static final String PARENT_SCHEMA_NAME = "ParentSchemaName";

    @Config(name = PARENT_SCHEMA_NAME, desc = "a test class")
    public class Parent {

        @Id(desc = "desc")
        private String id;
        @Config(desc = "prop1Desc")
        public String prop1 = "defaultValue";

        @Config(desc = "prop2Desc")
        public Set<String> prop2;

        @Config(desc = "prop3Desc")
        public List<Integer> prop3 = Arrays.asList(1, 2, 3);

        @Config(desc = "prop4Desc")
        public DateTime prop4;

        @Config(desc = "prop5Desc")
        public DurationTime prop5;
        @Config(desc = "prop6Desc")
        public Child prop6;
        @Config(desc = "prop7Desc")
        public List<Child> prop7;

        @Config(desc = "prop8Desc")
        public Byte prop8;

        @Config(desc = "prop9Desc")
        public Long prop9;

        @Config(desc = "prop10Desc")
        public Short prop10;
        @Config(desc = "prop11Desc")
        public Float prop11;
        @Config(desc = "prop12Desc")
        public Double prop12;
        @Config(desc = "prop13Desc")
        public Boolean prop13;
        @Config(desc = "prop14Desc")
        public TimeUnit prop14;
        @Config(desc = "prop15Desc")
        public URL prop15;
        @Config(desc = "prop16Desc")
        public File prop16;
        @Config(desc = "prop17Desc")
        public List<File> prop17;
        @Config(desc = "prop18Desc")
        public List<URL> prop18;
        @Config(desc = "prop19Desc")
        public List<TimeUnit> prop19;
        @Config(desc = "prop20Desc")
        public Map<String, Child> prop20;

        public void add(Child... c) {
            if (prop7 == null) {
                prop7 = new ArrayList<Child>();
            }
            prop7.addAll(Arrays.asList(c));
        }

        public List<Child> getChilds() {
            return prop7;
        }

        public void resetChilds() {
            prop7.clear();
        }

        public BeanId getId() {
            return BeanId.create(id, PARENT_SCHEMA_NAME);
        }

        public void set(Child c) {
            prop6 = c;
        }

        public void put(Child c) {
            if (prop20 == null) {
                prop20 = new HashMap<String, Child>();
            }
            prop20.put(c.id, c);
        }

        public Parent() {
        }

        public Parent(String id) {
            this.id = id;
        }

    }

    public static final String CHILD_SCHEMA_NAME = "ChildSchemaName";

    @Config(name = CHILD_SCHEMA_NAME, desc = "a test class")
    public class Child {

        @Id(desc = "desc")
        public String id;

        @Config(desc = "prop2Desc")
        public Set<String> prop2;

        @Config(desc = "prop3Desc")
        public List<Integer> prop3 = Arrays.asList(1, 2, 3);

        @Config(desc = "prop4Desc")
        public DateTime prop4;

        @Config(desc = "prop5Desc")
        public DurationTime prop5;

        @Config(desc = "prop8Desc")
        public Byte prop8;

        @Config(desc = "prop9Desc")
        public Long prop9;

        @Config(desc = "prop10Desc")
        public Short prop10;

        @Config(desc = "prop11Desc")
        public Float prop11;

        @Config(desc = "prop12Desc")
        public Double prop12;

        @Config(desc = "prop13Desc")
        public Boolean prop13;

        @Config(desc = "prop15Desc")
        public URL prop15;

        @Config(desc = "prop16Desc")
        public File prop16;

        @Config(desc = "prop17Desc")
        public List<File> prop17;

        @Config(desc = "prop18Desc")
        public List<URL> prop18;

        @Config(desc = "prop19Desc")
        public List<TimeUnit> prop19;

        public Child(String id) {
            this.id = id;
        }

        public BeanId getId() {
            return BeanId.create(id, CHILD_SCHEMA_NAME);
        }

        public Child() {

        }
    }

    public static final String SINGLETON_SCHEMA_NAME = "SingletonSchemaName";

    @Config(name = SINGLETON_SCHEMA_NAME, desc = "")
    public class Singleton {

        @Id(desc = "")
        public static final String id = "singleton";

        public BeanId getBeanId() {
            return BeanId.createSingleton(id, SINGLETON_SCHEMA_NAME);
        }
    }

    public static final String SINGLETON_PARENT_SCHEMA_NAME = "SingletonParentSchemaName";

    @Config(name = SINGLETON_PARENT_SCHEMA_NAME, desc = "")
    public class SingletonParent {

        @Id(desc = "")
        public String id = "singletonParent";

        @Config(desc = "")
        public Singleton singleton;

        public SingletonParent(String id) {
            this.id = id;
        }

        public SingletonParent() {

        }

    }

}

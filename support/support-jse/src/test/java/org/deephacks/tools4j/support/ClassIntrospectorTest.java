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
package org.deephacks.tools4j.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.deephacks.tools4j.support.reflections.ClassIntrospector;
import org.deephacks.tools4j.support.reflections.ClassIntrospector.FieldWrap;
import org.junit.Test;

public class ClassIntrospectorTest {
    @Test
    public void test1() {
        ClassIntrospector i = new ClassIntrospector(Test1.class);
        assertThat(i.getName(), is(Test1.class.getName()));

        assertThat(i.get(XmlRootElement.class).name(), is("name"));
        Map<String, FieldWrap<XmlElement>> fields = i.getFieldMap(XmlElement.class);

        FieldWrap<XmlElement> f = fields.get("var1");
        assertThat(f.getFieldName(), is("var1"));
        assertEquals(f.getType(), String.class);
        assertNull(f.getDefaultValue());

        f = fields.get("var2");
        assertThat(f.getFieldName(), is("var2"));
        assertEquals(f.getType(), Boolean.class);
        assertNull(f.getDefaultValue());

        f = fields.get("var3");
        assertThat(f.getFieldName(), is("var3"));
        assertTrue(f.isCollection());
        assertEquals(f.getCollRawType(), List.class);
        assertEquals(f.getType(), String.class);
        assertNull(f.getDefaultValues());

        f = fields.get("var4");
        assertThat(f.getFieldName(), is("var4"));
        assertTrue(f.isCollection());
        assertEquals(f.getCollRawType(), List.class);
        assertEquals(f.getType(), Test2.class);
        Collection<?> values = f.getDefaultValues();
        Test2 value = (Test2) values.iterator().next();
        assertThat(1.0, is(value.var1));
        assertThat(2, is(value.var2));
        assertThat(2, is(value.units.size()));

        f = fields.get("var5");
        assertThat(f.getFieldName(), is("var5"));
        assertTrue(f.isCollection());
        assertEquals(f.getCollRawType(), Set.class);
        assertEquals(f.getType(), Double.class);
        assertNull(f.getDefaultValue());
    }

    @XmlRootElement(name = "name")
    public class Test1 {
        @SuppressWarnings("unused")
        @XmlElement(name = "var1")
        private String var1;
        @SuppressWarnings("unused")
        @XmlElement(name = "var2")
        private Boolean var2;
        @SuppressWarnings("unused")
        @XmlElement(name = "var3")
        private List<String> var3;
        @SuppressWarnings("unused")
        @XmlElement(name = "var4")
        private List<Test2> var4 = Arrays.asList(new Test2());

        @SuppressWarnings("unused")
        @XmlElement(name = "var5")
        private Set<Double> var5;
    }

    public class Test2 {
        private Double var1 = 1.0;
        private Integer var2 = 2;
        private List<TimeUnit> units = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS);
    }
}

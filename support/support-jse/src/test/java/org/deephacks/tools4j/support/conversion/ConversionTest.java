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
package org.deephacks.tools4j.support.conversion;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.deephacks.tools4j.support.conversion.Conversion;
import org.deephacks.tools4j.support.types.DateTime;
import org.deephacks.tools4j.support.types.DurationTime;
import org.junit.Test;

public class ConversionTest {
    private Conversion conversion = Conversion.get();

    @Test
    public void testByte() {
        Byte val = new Byte((byte) 2);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2"));
        Byte obj = conversion.convert(str, Byte.class);
        assertEquals(obj, val);
    }

    @Test
    public void testInteger() {
        Integer val = new Integer(2);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2"));
        Object obj = conversion.convert(str, Integer.class);
        assertEquals(obj, val);
    }

    @Test
    public void testLong() {
        Long val = new Long(2);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2"));
        Long obj = conversion.convert(str, Long.class);
        assertEquals(obj, val);
    }

    @Test
    public void testShort() {
        Short val = new Short((short) 2);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2"));
        Short obj = conversion.convert(str, Short.class);
        assertEquals(obj, val);
    }

    @Test
    public void testFloat() {
        Float val = new Float(2.5);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2.5"));
        Float obj = conversion.convert(str, Float.class);
        assertEquals(obj, val);
    }

    @Test
    public void testDouble() {
        Double val = new Double(200.511111111111);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("200.511111111111"));
        Double obj = conversion.convert(str, Double.class);
        assertEquals(obj, val);
    }

    @Test
    public void testBoolean() {
        Boolean val = new Boolean(true);
        String str = conversion.convert(val, String.class);
        assertThat(str, is("true"));
        Boolean obj = conversion.convert(str, Boolean.class);
        assertEquals(obj, val);

        str = conversion.convert((String) null, String.class);
        assertNull(str);

        try {
            obj = conversion.convert("", Boolean.class);
            fail("only true and false is allowed: " + obj);
        } catch (Exception e) {
            assertTrue(true);
        }
        assertEquals(obj, val);

    }

    @Test
    public void testEnum() {
        TimeUnit val = TimeUnit.SECONDS;
        String str = conversion.convert(val, String.class);
        assertThat(str, is("SECONDS"));
        TimeUnit obj = conversion.convert(str, TimeUnit.class);
        assertEquals(obj, val);

        val = TimeUnit.HOURS;
        str = conversion.convert(val, String.class);
        assertThat(str, is("HOURS"));
        obj = conversion.convert(str, TimeUnit.class);
        assertEquals(obj, val);
    }

    @Test
    public void testURL() throws Exception {
        URL val = new URL("http://stoffe.deephacks.org");
        String str = conversion.convert(val, String.class);
        assertThat(str, is("http://stoffe.deephacks.org"));
        URL obj = conversion.convert(str, URL.class);
        assertEquals(obj, val);
    }

    @Test
    public void testFile() {
        File val = new File(".");
        String path = val.getAbsolutePath();
        String str = conversion.convert(val, String.class);
        assertThat(str, is("."));
        File obj = conversion.convert(str, File.class);
        assertEquals(obj.getAbsolutePath(), path);
    }

    @Test
    public void testDateTime() {
        DateTime val = new DateTime("2002-09-24-06:00");
        String str = conversion.convert(val, String.class);
        assertThat(str, is("2002-09-24-06:00"));
        DateTime obj = conversion.convert(str, DateTime.class);
        assertEquals(obj, val);
    }

    @Test
    public void testDurationTime() {
        DurationTime val = new DurationTime("PT12H30M5S");
        String str = conversion.convert(val, String.class);
        assertThat(str, is("PT12H30M5S"));
        DurationTime obj = conversion.convert(str, DurationTime.class);
        assertEquals(obj, val);

        assertThat(obj.getHours(), is(12));
        assertThat(obj.getMinutes(), is(30));
        assertThat(obj.getSeconds(), is((long) 5));
    }

}

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
package org.deephacks.tools4j.support.reflections;

import static org.deephacks.tools4j.support.reflections.Reflections.getParameterizedType;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GenericReflectionsTest {
    @SuppressWarnings("unused")
    private List<String> list;

    @Test
    public void testField() throws Exception {
        Field f = GenericReflectionsTest.class.getDeclaredField("list");
        assertThat(String.class.getName(), is(getParameterizedType(f).get(0).getName()));
    }

    public void testClass() {
        List<String> l = new ArrayList<String>();
        assertThat(String.class.getName(), is(getParameterizedType(l.getClass(), List.class).get(0)
                .getName()));
    }
}

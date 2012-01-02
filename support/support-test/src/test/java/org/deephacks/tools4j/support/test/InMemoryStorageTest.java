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
package org.deephacks.tools4j.support.test;

import static com.google.common.base.Predicates.not;
import static org.deephacks.tools4j.support.test.Criteria.equal;
import static org.deephacks.tools4j.support.test.Criteria.field;
import static org.deephacks.tools4j.support.test.Criteria.largerThan;
import static org.deephacks.tools4j.support.test.Criteria.lessThan;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deephacks.tools4j.support.test.Criteria;
import org.deephacks.tools4j.support.test.InMemoryStorage;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import com.google.common.base.Objects;

public class InMemoryStorageTest {
    InMemoryStorage storage = new InMemoryStorage();
    private final int numElements = 10;

    @Before
    public void setUp() {
        for (int i = 0; i < numElements; i++) {
            storage.add(Test1.class, new Test1(i));
            storage.add(Test2.class, new Test2(i));
        }
    }

    @Test
    public void testGetAll() {
        assertThat(storage.getAll(Test1.class).size(), is(numElements));
        assertThat(storage.getAll(Test2.class).size(), is(numElements));
    }

    @Test
    public void testSelect() {
        Criteria q = field("value").is(largerThan(1)).and(lessThan(8));
        Criteria p = field("value").is(not(equal(4)));
        Collection<Test1> result = storage.select(q.and(p)).from(Test1.class);
        ReflectionAssert.assertLenientEquals(getList(Test1.class, 2, 3, 5, 6, 7), result);
    }

    @Test
    public void testDelete() {
        Criteria q = field("value").is(largerThan(1)).and(lessThan(8));
        Criteria p = field("value").is(not(equal(4)));
        storage.delete(q.and(p)).from(Test1.class);
        Collection<Test1> result = storage.getAll(Test1.class);
        ReflectionAssert.assertLenientEquals(getList(Test1.class, 0, 1, 4, 8, 9), result);

    }

    private <T> List<T> getList(Class<T> clazz, Integer... values) {
        List<T> result = new ArrayList<T>();
        try {
            Constructor<T> c = clazz.getConstructor(Integer.class);
            for (Integer value : values) {
                result.add(c.newInstance(value));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;

    }

    private static class Test1 {
        private final Integer value;

        public Test1(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("value", value).add("class", Test1.class)
                    .toString();
        }
    }

    private static class Test2 {
        private final Integer value;

        public Test2(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("value", value).add("class", Test2.class)
                    .toString();
        }
    }
}

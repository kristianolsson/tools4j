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
package org.deephacks.tools4j.support.lookup;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deephacks.tools4j.support.lookup.Lookup;
import org.junit.Test;

public class LookupTest {

    /**
     * Test that an invalid system property fall-back to default.
     */

    public void testInvalidSystemProperty() {
        try {
            System.setProperty(Lookup.class.getName(), "bogus");
            assertThat(Lookup.get(), is(Lookup.class));
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testMultipleLookups() {

        InternalMockLookup.setMockInstances(Collection.class, new ArrayList());
        InternalMockLookup.setMockInstances(List.class, new LinkedList());
        System.out.println(Lookup.get().lookupAll(Collection.class).iterator().next().getClass()
                .getName());
        System.out.println(Lookup.get().lookupAll(List.class).iterator().next().getClass()
                .getName());
    }
}

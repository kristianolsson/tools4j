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

import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;

/**
 * The primary purpose InMemoryStorage is to provide a type-safe heterogeneous container storage of objects, 
 * supporting querying of objects and performing operations on them.
 */
public class InMemoryStorage {
    private final Multimap<Class<?>, Object> storage = ArrayListMultimap.create();

    /**
     * Add the object to the storage.
     * 
     * @param clazz The class of the object
     * @param object object to add.
     * @return true if added, false if the object was already present.
     */
    public <T extends Object> boolean add(Class<T> clazz, T object) {
        Preconditions.checkNotNull(object);
        return storage.put(clazz, object);
    }

    /**
     * Adds all the objects to the storage.
     * 
     * @param clazz The class of the objects
     * @param objects objects to add.
     * @return false if some object was already in the list.
     */
    public <T extends Object> boolean addAll(Class<T> clazz, Collection<T> objects) {
        Preconditions.checkNotNull(objects);
        return this.storage.putAll(clazz, objects);
    }

    /**
     * Finds objects that matches the query.
     * 
     * @param criteria used to match objects.
     * @return Operation to be performed on the matching objects.
     */
    @SuppressWarnings("unchecked")
    public Operation select(final Criteria criteria) {
        return new Operation() {

            @Override
            public <T> Collection<T> from(Class<T> clazz) {
                Collection<T> objects = getAll(clazz);
                return Collections2.filter(objects, criteria);
            }

        };
    }

    public Operation delete(final Criteria criteria) {
        return new Operation() {

            @Override
            public <T> Collection<T> from(Class<T> clazz) {
                Collection<T> toRemove = select(criteria).from(clazz);
                storage.get(clazz).removeAll(toRemove);
                return toRemove;
            }

        };
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> Collection<T> getAll(Class<T> clazz) {
        return (Collection<T>) storage.get(clazz);
    }

    public abstract static class Operation {
        public abstract <T> Collection<T> from(Class<T> clazz);
    }
}

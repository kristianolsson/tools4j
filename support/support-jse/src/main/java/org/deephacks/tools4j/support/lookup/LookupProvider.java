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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LookupProvider {
    protected final ConcurrentHashMap<Class<?>, Object> objectRegistry = new ConcurrentHashMap<Class<?>, Object>();

    /**
    * Look up an object matching a given interface.
    * 
    * @param clazz The type of the object we want to lookup.
    * @return The object, if found, otherwise null.
    */
    public abstract <T> T lookup(Class<T> clazz);

    /**
    * Look up a list of objects that match a given interface.
    * 
    * @param clazz The type of the object we want to lookup.
    * @return The object(s), if found, otherwise null.
    */
    public abstract <T> Collection<T> lookupAll(Class<T> clazz);

    /**
     * ServiceLoaderLookup is responsible for handling standard java service loader lookup.
     */
    static class ServiceLoaderLookup extends LookupProvider {

        public ServiceLoaderLookup() {

        }

        public final <T> T lookup(Class<T> clazz) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (T standardJavaProvider : java.util.ServiceLoader.load(clazz, cl)) {
                // return first provider found. May need more elaborate mechanism in future.
                if (standardJavaProvider == null) {
                    continue;
                }
                // return the provider that was first found.
                return standardJavaProvider;
            }
            return null;
        }

        @Override
        public <T> Collection<T> lookupAll(Class<T> clazz) {
            ArrayList<T> result = new ArrayList<T>();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (T o : java.util.ServiceLoader.load(clazz, cl)) {
                // return first provider found. May need more elaborate mechanism in future.
                result.add(o);
            }
            return result;
        }
    }
}

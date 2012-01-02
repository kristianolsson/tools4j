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

import org.deephacks.tools4j.support.lookup.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

/**
 * Lookup is responsible for solving the problem of dynamic service discovery. Service providers 
 * register themselves and clients query for a suitable provider, without knowing how lookup is 
 * performed. The purpose is to achieve modularity and separation between components.
 */
public class Lookup extends LookupProvider {
    private static Logger LOG = LoggerFactory.getLogger(Lookup.class);
    private ArrayList<LookupProvider> lookupProviders;
    private static Lookup LOOKUP;

    protected Lookup() {
        lookupProviders = new ArrayList<LookupProvider>();
    }

    /**
     * Aquire the Lookup registry.
     * @return The lookup registry. 
     */
    public static Lookup get() {
        if (LOOKUP != null) {
            return LOOKUP;
        }
        synchronized (Lookup.class) {
            // allow for override of the Lookup.class
            String overrideClassName = System.getProperty(Lookup.class.getName());
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            try {
                if (overrideClassName != null && !"".equals(overrideClassName)) {
                    LOOKUP = (Lookup) Class.forName(overrideClassName, true, l).newInstance();
                    LOG.trace(Constants.LOG_MSG_LOOKUP_OVERRIDE(overrideClassName));
                } else {
                    LOOKUP = new Lookup();
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Constants.LOG_MSG_LOOKUP_SYS_ERROR(overrideClassName), e);
                }
                LOG.warn(Constants.LOG_MSG_LOOKUP_SYS_ERROR(overrideClassName));
            }
            ServiceLoaderLookup serviceLoaderLookup = new ServiceLoaderLookup();
            LOOKUP.lookupProviders.add(serviceLoaderLookup);
            Collection<LookupProvider> providers = serviceLoaderLookup
                    .lookupAll(LookupProvider.class);
            LOOKUP.lookupProviders.addAll(providers);
            LOG.debug("{}", LOOKUP);
        }

        return LOOKUP;
    }

    @Override
    public <T> Collection<T> lookupAll(Class<T> clazz) {
        ArrayList<T> result = new ArrayList<T>();
        for (LookupProvider lp : lookupProviders) {
            result.addAll(lp.lookupAll(clazz));
        }
        return result;
    }

    public <T> T lookup(Class<T> clazz) {
        for (LookupProvider lp : lookupProviders) {
            T result = lp.lookup(clazz);
            if (result != null)
                return result;
        }
        return null;
    }

    public String toString() {
        return Objects.toStringHelper(Lookup.class).add("LOOKUP", LOOKUP.getClass().getName())
                .add("lookupProviders", lookupProviders).toString();
    }
}

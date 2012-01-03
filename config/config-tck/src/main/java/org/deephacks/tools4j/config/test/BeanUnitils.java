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

import java.util.Collection;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.support.conversion.Conversion;

import com.google.common.collect.Lists;

/**
 * 
 * This class requires that classes have already been registered.
 */
public class BeanUnitils {
    private static Conversion CONVERSION = Conversion.get();
    static {
        CONVERSION.register(new ObjectToBeanConverter());
    }

    /**
     * Convert a configurable bean instance into a Bean that can be provisioned directly
     * to the admin context in a type safe manner to avoid mistakes in Junit tests. 
     * 
     * WARNING: This method can steal quite a bit of performance, use sparingly. 
     * 
     * @param bean A bean instance of the real object.
     * @return 
     */
    public static Collection<Bean> toBeans(Object... objects) {
        return CONVERSION.convert(Lists.newArrayList(objects), Bean.class);
    }

    public static Bean toBean(Object object) {
        return CONVERSION.convert(object, Bean.class);
    }
}

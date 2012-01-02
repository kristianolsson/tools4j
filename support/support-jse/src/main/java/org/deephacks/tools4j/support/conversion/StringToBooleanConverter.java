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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringToBooleanConverter implements Converter<String, Boolean> {
    private static final Set<String> trueValues = new HashSet<String>();
    private static final Set<String> falseValues = new HashSet<String>();

    static {
        trueValues.addAll(Arrays.asList("true", "on", "yes", "y", "1"));
        falseValues.addAll(Arrays.asList("false", "off", "no", "n", "0"));
    }

    @Override
    public Boolean convert(String source, Class<? extends Boolean> specificType) {
        String value = source.trim();
        if (trueValues.contains(value)) {
            return Boolean.TRUE;
        } else if (falseValues.contains(value)) {
            return Boolean.FALSE;
        } else {
            throw new ConversionException("Invalid boolean value '" + source + "'");
        }
    }

}

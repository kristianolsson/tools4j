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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class can convert all number types such as BigDecimal, BigInteger, Byte, Double, 
 * Float, Integer, Long, and Short.
 */
public class StringToNumberConverter implements Converter<String, Number> {

    @Override
    public Number convert(String source, Class<? extends Number> specificType) {
        String value = source.trim();
        try {
            if (specificType.equals(Byte.class)) {
                return Byte.valueOf(value);
            } else if (specificType.equals(Short.class)) {
                return Short.valueOf(value);
            } else if (specificType.equals(Integer.class)) {
                return Integer.valueOf(value);
            } else if (specificType.equals(Long.class)) {
                return Long.valueOf(value);
            } else if (specificType.equals(BigInteger.class)) {
                return new BigInteger(value);
            } else if (specificType.equals(Float.class)) {
                return Float.valueOf(value);
            } else if (specificType.equals(Double.class)) {
                return Double.valueOf(value);
            } else if (specificType.equals(BigDecimal.class) || specificType.equals(Number.class)) {
                return new BigDecimal(value);
            }
            throw new ConversionException("Cannot convert [" + source + "] to ["
                    + specificType.getName() + "]");

        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert [" + source + "] to ["
                    + specificType.getName() + "]", e);
        }

    }

}

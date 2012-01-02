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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This class can convert any enum to a string.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class StringToEnumConverter implements Converter<String, Enum> {

    @Override
    public Enum convert(String source, Class<? extends Enum> specificType) {
        try {
            return Enum.valueOf(specificType, source);
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Could not convert value [" + source
                    + "] to any of the possible values:  " + getPossibleValueString(specificType)
                    + ".");
        }
    }

    public String getPossibleValueString(Class<?> clazz) {
        StringBuffer sb = new StringBuffer();
        Field[] fields = clazz.getDeclaredFields();
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEnumConstant()) {
                try {
                    Object aEnum = fields[i].get(null);
                    values.add(aEnum.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.get(i));
            if ((i + 1) != values.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}

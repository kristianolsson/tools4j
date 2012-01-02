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

/**
 * This is the fallback string converter that simply does a toString on the
 * provided object.
 * 
 * Works fine for Number, Boolean, Enums and all other values that have a 
 * toString that represent their real values in a serialized form.
 */
public class ObjectToStringConverter implements Converter<Object, String> {

    @Override
    public String convert(Object source, Class<? extends String> specificType) {
        return (source != null ? source.toString() : null);
    }

}

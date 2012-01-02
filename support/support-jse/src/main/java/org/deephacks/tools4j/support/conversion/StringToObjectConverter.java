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

import static org.deephacks.tools4j.support.reflections.Reflections.getConstructor;
import static org.deephacks.tools4j.support.reflections.Reflections.getStaticMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General purpose converter that is able to convert a String to an object if the 
 * object have a suitable static valueof method or a single argument String constructor.
 * 
 * This should work fine for File, URL, DateTime, DurationTime
 */
public class StringToObjectConverter implements Converter<String, Object> {
    private Logger logger = LoggerFactory.getLogger(StringToObjectConverter.class);

    @Override
    public Object convert(String source, Class<? extends Object> specificType) {
        Method valueof = getStaticMethod(specificType, "valueof", String.class);

        try {
            if (valueof != null) {
                valueof.setAccessible(true);
                return valueof.invoke(null, source);
            }
            Constructor<?> cons = getConstructor(specificType, String.class);
            if (cons != null) {
                cons.setAccessible(true);
                return cons.newInstance(source);
            }
        } catch (InvocationTargetException e) {
            logger.debug("StringToObjectConverter exception. Source: {} Target: {}", source,
                    specificType);
            throw new ConversionException(e.getTargetException());
        } catch (Throwable e) {
            throw new ConversionException(e);
        }
        throw new UnsupportedOperationException(
                "No static valueOf(String.class) method or Constructor(String.class) exists on "
                        + specificType.getName());
    }

}

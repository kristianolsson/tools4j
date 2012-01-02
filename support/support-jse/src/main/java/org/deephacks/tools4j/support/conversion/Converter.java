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
 * Converts a object of type V to a object of type T.
 * 
 * Both V and T can be a super class or interface that handles a range of 
 * subclasses.
 * 
 * The algorithm for finding a suitable converter begins by first finding 
 * converters that are able to convert both source and target; a exact or 
 * superclass match. The final decision falls on the converter that have 
 * the best target match.  
 * 
 * That is, the converter that is most specialized in converting a value T to 
 * a specific target class will be prioritized, as long as it recognizes 
 * the source value V. 
 * 
 * Converter providers are regsitered using the standard java service provider 
 * mechanism.
 * 
 * @author Kristoffer Sjogren
 */
public interface Converter<V, T> {
    /**
     * @param source The source value to convert.
     * @param the most specific type that the value should be converted to.  
     * @return A converted object.
     */
    public T convert(V source, Class<? extends T> specificType);

}

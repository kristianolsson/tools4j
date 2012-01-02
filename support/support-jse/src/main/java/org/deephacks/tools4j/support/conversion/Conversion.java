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

import static com.google.common.base.Objects.equal;
import static org.deephacks.tools4j.support.reflections.Reflections.getParameterizedType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deephacks.tools4j.support.lookup.Lookup;

import com.google.common.base.Objects;

/**
 * Conversion is responsible for converting values using registered converters. 
 * 
 * Converters can be regsitered using that standard java service provider mechanism, using
 * the Converter interface.
 * 
 * @author Kristoffer Sjogren
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Conversion {
    private HashMap<Class<?>, SourceTargetPair> converters = new HashMap<Class<?>, SourceTargetPair>();

    private ConcurrentHashMap<SourceTargetPairKey, Converter> cache = new ConcurrentHashMap<SourceTargetPairKey, Converter>();

    private static Conversion INSTANCE;

    private Conversion() {
        registerDefault();
        registerSpi();
    }

    public static synchronized Conversion get() {
        if (INSTANCE == null) {
            INSTANCE = new Conversion();

        }
        return INSTANCE;
    }

    /**
     * Convert a value to a specific class. 
     * 
     * The algorithm for finding a suitable converter is as follows: 
     * 
     * Find converters that is able to convert both source and target; a exact or 
     * superclass match. Pick the converter that have the best target match, if both
     * are equal, pick the one with best source match.  
     * 
     * That is, the converter that is most specialized in converting a value to 
     * a specific target class will be prioritized, as long as it recognizes the source 
     * value. 
     * 
     * @param source value to convert.
     * @param targetclass class to convert to.
     * @return converted value
     */
    public <T> T convert(final Object source, final Class<T> targetclass) {
        if (source == null) {
            return null;
        }

        Class<?> sourceclass = source.getClass();
        SourceTargetPairKey key = new SourceTargetPairKey(sourceclass, targetclass);
        Converter converter = cache.get(key);

        if (converter != null) {
            converter.convert(source, targetclass);
        }
        LinkedList<SourceTargetPairMatch> matches = new LinkedList<SourceTargetPairMatch>();
        for (SourceTargetPair pair : converters.values()) {
            SourceTargetPairMatch match = pair.match(sourceclass, targetclass);
            if (match.matchesSource() && match.matchesTarget()) {
                matches.add(match);
            }
        }
        if (matches.size() == 0) {
            throw new ConversionException("No suitable converter found for target class ["
                    + targetclass.getName() + "] and source value [" + sourceclass.getName()
                    + "]. The following converters are available [" + converters + "]");
        }

        Collections.sort(matches, SourceTargetPairMatch.bestTargetMatch());
        converter = matches.get(0).pair.converter;
        cache.put(key, converter);
        return (T) converter.convert(source, targetclass);

    }

    public <T, V> Collection<T> convert(Collection<V> values, final Class<T> clazz) {
        ArrayList<T> objects = new ArrayList<T>();
        if (values == null) {
            return new ArrayList<T>();
        }
        for (V object : values) {
            objects.add(convert(object, clazz));
        }
        return objects;
    }

    public <T, V> Map<V, T> convert(Map<V, Object> values, final Class<T> clazz) {
        if (values == null) {
            return null;
        }
        throw new UnsupportedOperationException();
    }

    public <T, V> void register(Converter converter) {
        if (converters.get(converter.getClass()) != null) {
            return;
        }
        converters.put(converter.getClass(), new SourceTargetPair(converter));
        cache.clear();

    }

    private void registerSpi() {
        for (Converter converter : Lookup.get().lookupAll(Converter.class)) {
            register(converter);
        }
    }

    private void registerDefault() {
        register(new StringToEnumConverter());
        register(new StringToObjectConverter());
        register(new ObjectToStringConverter());
        register(new StringToNumberConverter());
        register(new StringToBooleanConverter());
    }

    private static class SourceTargetPair {
        private Class<?> source;
        private Class<?> target;
        private Converter converter;

        public SourceTargetPair(Converter converter) {
            List<Class<?>> types = getParameterizedType(converter.getClass(), Converter.class);
            if (types.size() < 2) {
                throw new IllegalArgumentException(
                        "Unable to the determine generic source and target type "
                                + "for converter. Please declare these generic types.");
            }
            this.source = types.get(0);
            this.target = types.get(1);
            this.converter = converter;
        }

        public SourceTargetPairMatch match(Class<?> sourceValueClass, Class<?> targetClass) {
            return new SourceTargetPairMatch(this, getSourceMatchDistance(sourceValueClass),
                    getTargetMatchDistance(targetClass));
        }

        /**
         * Returns a list of classes that matches the candidate in terms 
         * of converter source. The list is sorted with the most specific match first. 
         */
        private int getSourceMatchDistance(Class<?> candidate) {
            return distance(candidate, source);
        }

        /**
         * Returns a list of classes that matches the candidate in terms 
         * of converter target. The list is sorted with the most specific match first. 
         */
        private int getTargetMatchDistance(Class<?> candidate) {
            return distance(candidate, target);
        }

        /**
         * Climb the class hierarchy of the candidate class and calculate the distance 
         * between to the capability class. 
         * 
         * @return The distance in the class hierarchy between the candidate and capability.
         */
        private int distance(Class<?> candidate, Class<?> capability) {
            int distance = 0;
            if (candidate.equals(capability)) {
                return distance;
            }
            LinkedList<Class<?>> superclasses = new LinkedList<Class<?>>();
            superclasses.add(candidate.getSuperclass());
            while (!superclasses.isEmpty()) {
                Class<?> candidateSuperclazz = superclasses.removeLast();
                if (candidateSuperclazz.equals(capability)) {
                    if (capability == Object.class) {
                        // Object converters are absolute last resort
                        return Integer.MAX_VALUE;
                    }
                    return ++distance;
                }
                addInterfaces(candidateSuperclazz, superclasses);
                if (candidateSuperclazz.getSuperclass() != null) {
                    superclasses.add(candidateSuperclazz.getSuperclass());
                }
            }
            // no match
            return -1;
        }

        private void addInterfaces(Class<?> clazz, LinkedList<Class<?>> superclasses) {
            for (Class<?> inheritedIfc : clazz.getInterfaces()) {
                addInterfaces(inheritedIfc, superclasses);
            }
        }
    }

    private static class SourceTargetPairMatch {
        private int bestTargetMatch = -1;
        private int bestSourceMatch = -1;
        private SourceTargetPair pair;

        public SourceTargetPairMatch(SourceTargetPair pair, int bestSourceMatch, int bestTargetMatch) {
            this.pair = pair;
            this.bestSourceMatch = bestSourceMatch;
            this.bestTargetMatch = bestTargetMatch;
        }

        public boolean matchesTarget() {
            return (bestTargetMatch > -1 ? true : false);
        }

        public boolean matchesSource() {
            return (bestSourceMatch > -1 ? true : false);
        }

        public static Comparator<SourceTargetPairMatch> bestTargetMatch() {
            return new Comparator<Conversion.SourceTargetPairMatch>() {

                @Override
                public int compare(SourceTargetPairMatch o1, SourceTargetPairMatch o2) {
                    if (o1.bestTargetMatch < o2.bestTargetMatch) {
                        return -1;
                    } else if (o1.bestTargetMatch > o2.bestTargetMatch) {
                        return 1;
                    }
                    // equal target, pick best source.
                    if (o1.bestSourceMatch < o2.bestSourceMatch) {
                        return -1;
                    } else if (o1.bestSourceMatch > o2.bestSourceMatch) {
                        return 1;
                    }
                    return 0;
                }
            };
        }
    }

    private static class SourceTargetPairKey {
        Class<?> source;
        Class<?> target;

        public SourceTargetPairKey(Class<?> source, Class<?> target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(source, target);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceTargetPairKey)) {
                return false;
            }
            SourceTargetPairKey other = (SourceTargetPairKey) obj;
            return equal(source, other.source) && equal(target, other.target);
        }
    }
}

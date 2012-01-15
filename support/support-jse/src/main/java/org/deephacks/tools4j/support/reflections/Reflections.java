package org.deephacks.tools4j.support.reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * General purpose class for commonly used reflection operations.
 * 
 * @author Kristoffer Sjogren
 */
public class Reflections {
    public static Class<?> forName(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all superclasses and interfaces recursively.
     *
     * @param clazz The class to start the search with.
     *
     * @return List of all super classes and interfaces of {@code clazz}. The list contains the class itself! The empty
     *         list is returned if {@code clazz} is {@code null}.
     */
    public static List<Class<?>> computeClassHierarchy(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        computeClassHierarchy(clazz, classes);
        return classes;
    }

    /**
     * Get all superclasses and interfaces recursively.
     *
     * @param clazz The class to start the search with.
     * @param classes List of classes to which to add all found super classes and interfaces.
     */
    private static void computeClassHierarchy(Class<?> clazz, List<Class<?>> classes) {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            if (classes.contains(current)) {
                return;
            }
            classes.add(current);
            for (Class<?> currentInterface : current.getInterfaces()) {
                computeClassHierarchy(currentInterface, classes);
            }
        }
    }

    public static <T> T newInstance(Class<T> type) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> enclosing = type.getEnclosingClass();
        if (enclosing == null) {
            Constructor<?> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return type.cast(c.newInstance());
        }
        Object o = enclosing.newInstance();
        Constructor<?> cc = type.getDeclaredConstructor(enclosing);
        cc.setAccessible(true);
        return type.cast(cc.newInstance(o));

    }

    public static Object newInstance(String clazzName, String value) {
        try {
            Class<?> type = forName(clazzName);
            Class<?> enclosing = type.getEnclosingClass();
            if (enclosing == null) {
                Constructor<?> c = type.getConstructor(String.class);
                c.setAccessible(true);
                return type.cast(c.newInstance(value));
            }
            Object o = enclosing.newInstance();
            Constructor<?> c = type.getDeclaredConstructor(enclosing, String.class);
            c.setAccessible(true);
            return type.cast(c.newInstance(o, value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getComponentType(T[] a) {
        Class<?> k = a.getClass().getComponentType();
        return (Class<? extends T>) k; // unchecked cast
    }

    public static <T> T[] newArray(T[] a, int size) {
        return newArray(getComponentType(a), size);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<? extends T> k, int size) {
        if (k.isPrimitive())
            throw new IllegalArgumentException("Argument cannot be primitive: " + k);
        Object a = java.lang.reflect.Array.newInstance(k, size);
        return (T[]) a; // unchecked cast
    }

    public static <T> T cast(Class<T> clazz, Object object) {
        if (object == null) {
            return null;
        }
        if (clazz.isAssignableFrom(object.getClass())) {
            return clazz.cast(object);
        }
        return null;
    }

    /**
     * Find all field (including private) on a specific class. Searches all
     * super-classes up to {@link Object}.
     * 
     * @param clazz
     *            Class to inspect
     * @return all found fields.
     */
    public static List<Field> findFields(final Class<?> clazz) {
        List<Field> foundFields = new ArrayList<Field>();
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && (searchType != null)) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                foundFields.add(field);
            }
            searchType = searchType.getSuperclass();
        }
        return foundFields;
    }

    public static Multimap<Class<? extends Annotation>, Field> findFieldsAnnotations(
            List<Field> fields) {
        Multimap<Class<? extends Annotation>, Field> fieldAnnotations = ArrayListMultimap.create();
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                fieldAnnotations.put(annotation.annotationType(), field);
            }
        }
        return fieldAnnotations;

    }

    /**
     * Returns the parameterized type of a field, if exists. Wild cards, type
     * variables and raw types will be returned as an empty list.
     * <p>
     * If a field is of type Set<String> then java.lang.String is returned.
     * </p>
     * <p>
     * If a field is of type Map<String, Integer> then [java.lang.String,
     * java.lang.Integer] is returned.
     * </p>
     * 
     * @param field
     * @return A list of classes of the parameterized type.
     */
    public static List<Class<?>> getParameterizedType(final Field field) {
        Type type = field.getGenericType();

        if (!ParameterizedType.class.isAssignableFrom(type.getClass())) {

            // the field is it a raw type and does not have generic type
            // argument. Return empty list.
            return new ArrayList<Class<?>>();
        }

        ParameterizedType ptype = (ParameterizedType) type;
        Type[] targs = ptype.getActualTypeArguments();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Type aType : targs) {

            if (Class.class.isAssignableFrom(aType.getClass())) {
                classes.add((Class<?>) aType);
            } else if (WildcardType.class.isAssignableFrom(aType.getClass())) {
                // wild cards are not handled by this method
            } else if (TypeVariable.class.isAssignableFrom(aType.getClass())) {
                // type variables are not handled by this method
            }
        }
        return classes;
    }

    /**
     * Returns the parameterized type of a class, if exists. Wild cards, type
     * variables and raw types will be returned as an empty list.
     * <p>
     * If a field is of type Set<String> then java.lang.String is returned.
     * </p>
     * <p>
     * If a field is of type Map<String, Integer> then [java.lang.String,
     * java.lang.Integer] is returned.
     * </p>
     * 
     * @param ownerClass the implementing target class to check against
     * @param the generic interface to resolve the type argument from
     * @return A list of classes of the parameterized type.
     */
    public static List<Class<?>> getParameterizedType(final Class<?> ownerClass,
            Class<?> genericSuperClass) {
        Type[] types = null;
        if (genericSuperClass.isInterface()) {
            types = ownerClass.getGenericInterfaces();
        } else {
            types = new Type[] { ownerClass.getGenericSuperclass() };
        }

        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Type type : types) {

            if (!ParameterizedType.class.isAssignableFrom(type.getClass())) {
                // the field is it a raw type and does not have generic type
                // argument. Return empty list.
                return new ArrayList<Class<?>>();
            }

            ParameterizedType ptype = (ParameterizedType) type;
            Type[] targs = ptype.getActualTypeArguments();

            for (Type aType : targs) {

                classes.add(extractClass(ownerClass, aType));
            }
        }
        return classes;
    }

    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getMethod(methodName, args);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... paramTypes) {
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
        if (arg instanceof ParameterizedType) {
            return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
        } else if (arg instanceof GenericArrayType) {
            throw new UnsupportedOperationException("GenericArray types are not supported.");
        } else if (arg instanceof TypeVariable) {
            throw new UnsupportedOperationException("GenericArray types are not supported.");
        }
        return (arg instanceof Class ? (Class<?>) arg : Object.class);
    }

}

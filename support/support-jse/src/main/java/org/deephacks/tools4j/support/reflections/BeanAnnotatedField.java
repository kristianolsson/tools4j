package org.deephacks.tools4j.support.reflections;

import static org.deephacks.tools4j.support.reflections.Reflections.getParameterizedType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public class BeanAnnotatedField<T extends Annotation> {
    private Field field;
    private T annotation;
    private Object target;

    BeanAnnotatedField(Field field, T annotation, Object target) {
        this.field = field;
        this.annotation = annotation;
        this.target = target;
    }

    public Class<?> getType() {
        return field.getType();
    }

    public String getTypeString() {
        StringBuffer sb = new StringBuffer();
        sb.append(field.getType().getCanonicalName());
        List<Class<?>> argument = getParameterizedType(field);
        if (argument.size() == 0) {
            return sb.toString();
        }

        sb.append("<");
        for (int i = 0; i < argument.size(); i++) {
            sb.append(argument.get(i).getCanonicalName());
            if ((i + 1) != argument.size()) {
                sb.append(", ");
            }
        }
        sb.append(">");
        return sb.toString();
    }

    public String getName() {
        return field.getName();
    }

    public T getAnnotation() {
        return annotation;
    }

    public Object getTarget() {
        return target;
    }

}

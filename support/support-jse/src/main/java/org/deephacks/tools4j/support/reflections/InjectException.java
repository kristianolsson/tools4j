package org.deephacks.tools4j.support.reflections;

public class InjectException extends RuntimeException {
    private static final long serialVersionUID = -7255806054007327447L;
    private BeanAnnotatedField<?> field;
    private Object value;

    public InjectException(String reason, BeanAnnotatedField<?> field, Object value) {
        super(reason);
        this.field = field;
        this.value = value;
    }

    public Object getIncorrectValue() {
        return value;
    }

    public BeanAnnotatedField<?> getField() {
        return field;
    }

}

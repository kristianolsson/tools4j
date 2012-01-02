package org.deephacks.tools4j.log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MessageSource {
    private ConcurrentMap<String, String> messageFieldValues = new ConcurrentHashMap<String, String>();

    protected MessageSource() {

    }

    private static final String SEPARATOR = "-";

    /**
     * If we want, we can localize this message constants later.
     */
    protected String getMessage(String constant, String... args) {
        String message = messageFieldValues.get(constant);
        if (message != null) {
            return MessageFormat.format(message, args);
        }
        try {
            for (Field f : this.getClass().getDeclaredFields()) {

                f.setAccessible(true);
                if (!this.getClass().isAnnotationPresent(Category.class)) {
                    throw new RuntimeException(
                            "@Category must be annotated on MessageSource classes.");
                }
                if (!f.isAnnotationPresent(Message.class)) {
                    continue;
                }
                if (!Modifier.isStatic(f.getModifiers())) {
                    throw new RuntimeException("@Message fields must be static.");
                }
                if (!Modifier.isFinal(f.getModifiers())) {
                    throw new RuntimeException("@Message fields must be final.");
                }
                if (!String.class.isAssignableFrom(f.getType())) {
                    throw new RuntimeException("@Message fields must be String Typed.");
                }
                if (!f.get(null).equals(constant)) {
                    continue;
                }
                Message msg = f.getAnnotation(Message.class);
                Category category = this.getClass().getAnnotation(Category.class);
                message = new StringBuilder().append(category.value()).append(SEPARATOR)
                        .append(msg.id()).append(" ").append(constant).toString();

                messageFieldValues.put(constant, message);

                return MessageFormat.format(message, args);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not find any fields annotated with @Message.");
    }
}

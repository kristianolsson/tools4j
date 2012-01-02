package org.deephacks.tools4j.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Message {

    /**
     * A application unique identifier of the message.
     */
    String id();

    /**
     * A description of the message.
     */
    String desc();

}

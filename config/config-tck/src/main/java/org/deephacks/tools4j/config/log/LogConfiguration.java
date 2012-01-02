package org.deephacks.tools4j.config.log;

import java.io.File;
import java.lang.reflect.Method;

import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The purpose of this class is to enable late load of configuration file and implementation binding 
 * of SLF4J.
 * 
 * SLF4J will automatically force the logback if it is available on class path.
 * 
 * If other providers are to be used, the maven dependency class path has to be arranged as 
 * illustrated below, i.e. exchanging logback for slf4j-log4j12-1.6.1.jar or slf4j-jdk14-1.6.1.jar.
 */
public class LogConfiguration {
    /**
     * logback-classic-0.9.24.jar + logback-core-0.9.24.jar
     */
    private static final String LOGBACK_IMPL = "ch.qos.logback.classic";
    private static final String LOGBACK_CONFIG = "org.deephacks.openconfig.internal.log.LogbackConfiguration";
    private static final String LOGBACK_FILE = "logback.xml";
    /**
     * slf4j-log4j12-1.6.1.jar + log4j-1.2.16.jar
     */
    private static final String LOG4J_IMPL = "org.slf4j.impl.Log4jLoggerFactory";
    private static final String LOG4J_CONFIG = " org.deephacks.openconfig.internal.log.Log4jConfiguration";
    private static final String LOG4J_FILE = "log4j.xml";
    /**
     * slf4j-jdk14-1.6.1.jar
     */
    private static final String JDK14_IMPL = "org.slf4j.impl.JDK14LoggerFactory";
    private static final String JDK14_CONFIG = " org.deephacks.openconfig.internal.log.JDK14Configuration";
    private static final String JDK14_FILE = "logging.properties";

    public static void init(Level level) {
        Logger root = (Logger) LoggerFactory.getLogger("org.deephacks");
        root.setLevel(level);
    }

    public static void init() {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("").getFile());
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        Method init = null;
        try {
            if (binder.getLoggerFactoryClassStr().startsWith(LOGBACK_IMPL)) {
                init = Class.forName(LOGBACK_CONFIG).getDeclaredMethod("init", File.class);
                init.invoke(null, new File(file, LOGBACK_FILE));
            } else if (binder.getLoggerFactoryClassStr().startsWith(LOG4J_IMPL)) {
                init = Class.forName(LOG4J_CONFIG).getDeclaredMethod("init", File.class);
                init.invoke(null, new File(file, LOG4J_FILE));
            } else if (binder.getLoggerFactoryClassStr().startsWith(JDK14_IMPL)) {
                init = Class.forName(JDK14_CONFIG).getDeclaredMethod("init", File.class);
                init.invoke(null, new File(file, JDK14_FILE));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

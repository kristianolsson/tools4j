package org.deephacks.tools4j.log;

import java.io.File;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Enable late load of configuration file and implementation binding of SLF4J.
 */
public class LogConfiguration {
    /**
     * logback-classic-0.9.24.jar + logback-core-0.9.24.jar
     */
    private static final String LOGBACK_IMPL = "ch.qos.logback.classic";
    private static final String LOGBACK_CONFIG = "org.deephacks.tools4j.log.LogbackConfiguration";
    private static final String LOGBACK_FILE = "logback.xml";
    /**
     * slf4j-log4j12-1.6.1.jar + log4j-1.2.16.jar
     */
    private static final String LOG4J_IMPL = "org.slf4j.impl.Log4jLoggerFactory";
    private static final String LOG4J_CONFIG = "org.deephacks.tools4j.log.Log4jConfiguration";
    private static final String LOG4J_FILE = "log4j.xml";
    /**
     * slf4j-jdk14-1.6.1.jar
     */
    private static final String JDK14_IMPL = "org.slf4j.impl.JDK14LoggerFactory";
    private static final String JDK14_CONFIG = "org.deephacks.tools4j.log.JDK14Configuration";
    private static final String JDK14_FILE = "logging.properties";

    private static Class<?> IMPL_IN_USE = null;
    private static File FILE = null;
    private static Logger LOGGER = null;

    public static synchronized void init(File file) {
        if (IMPL_IN_USE != null) {
            return;
        }
        StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        Method init = null;
        try {
            if (binder.getLoggerFactoryClassStr().startsWith(LOGBACK_IMPL)) {
                IMPL_IN_USE = Class.forName(LOGBACK_CONFIG);
                init = IMPL_IN_USE.getDeclaredMethod("init", File.class);
                FILE = new File(file, LOGBACK_FILE);
                init.invoke(null, FILE);
            } else if (binder.getLoggerFactoryClassStr().startsWith(LOG4J_IMPL)) {
                IMPL_IN_USE = Class.forName(LOG4J_CONFIG);
                init = IMPL_IN_USE.getDeclaredMethod("init", File.class);
                FILE = new File(file, LOG4J_FILE);
                init.invoke(null, FILE);
            } else if (binder.getLoggerFactoryClassStr().startsWith(JDK14_IMPL)) {
                IMPL_IN_USE = Class.forName(JDK14_CONFIG);
                init = IMPL_IN_USE.getDeclaredMethod("init", File.class);
                FILE = new File(file, JDK14_FILE);
                init.invoke(null, FILE);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOGGER = LoggerFactory.getLogger(LogConfiguration.class);
    }

    public static void setDebug() {
        if (IMPL_IN_USE == null) {
            throw new IllegalArgumentException("Logger have not been initialized yet.");
        }
        try {
            Method setDebug = IMPL_IN_USE.getDeclaredMethod("setDebug");
            setDebug.invoke(null);
            LOGGER.debug("LogLevel set to DEBUG for [{}] using file [{}].", IMPL_IN_USE.getName(),
                    FILE.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

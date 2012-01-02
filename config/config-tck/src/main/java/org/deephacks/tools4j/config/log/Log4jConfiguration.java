package org.deephacks.tools4j.config.log;

import java.io.File;

public class Log4jConfiguration {
    static void init(File file) {
        throw new RuntimeException("SLFJ cannot change log providers dynamically. Its a class loader thing.");
        // this code should work removing logback and add slf4j-log4j12-1.6.1.jar + log4j-1.2.16.jar as maven deps.
        //        try {
        //            InputStream input = new FileInputStream(file);
        //            new DOMConfigurator().doConfigure(input, LogManager.getLoggerRepository());
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        }
    }
}

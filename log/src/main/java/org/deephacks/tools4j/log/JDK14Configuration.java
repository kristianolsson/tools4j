package org.deephacks.tools4j.log;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.LogManager;

class JDK14Configuration {
    static void init(File file) {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void setDebug() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}

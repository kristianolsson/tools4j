package org.deephacks.tools4j.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jConfiguration {
    static void init(File file) {
        try {
            InputStream input = new FileInputStream(file);
            new DOMConfigurator().doConfigure(input, LogManager.getLoggerRepository());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void setDebug() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}

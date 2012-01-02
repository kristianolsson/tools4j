package org.deephacks.tools4j.config.log;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.LogManager;

import org.deephacks.tools4j.support.io.FileUtils;

class JDK14Configuration {
    static void init(File file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            LogManager.getLogManager().readConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.close(is);
        }
    }
}

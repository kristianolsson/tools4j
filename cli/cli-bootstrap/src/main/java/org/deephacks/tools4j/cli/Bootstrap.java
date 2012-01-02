package org.deephacks.tools4j.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Using .bat and .sh scripts to dynamically load the class path is a pain, so
 * we use this class to load classes using class loaders instead.
 * 
 * This bootstrap stuff needs to be in a separate jar, therefore it is also a
 * separate Maven project.
 * 
 * Make sure that no dependencies to other classes exist in this Bootstrap
 * class.
 * 
 * @author Kristoffer Sjogren
 */
public class Bootstrap {
    /**
     * This is set in the bat and sh scripts.
     */
    static final String CLI_HOME_VARIABLE = "org.deephacks.tools4j.cli.home";
    static final String LIB_DIR = "lib";
    static final String BOOT_DIR = "boot";

    /**
     * Main class in cli-api
     */
    private static final String MAIN_CLASS = "org.deephacks.tools4j.cli.CliMain";

    public static final void main(String[] args) {

        String home = System.getProperty(CLI_HOME_VARIABLE);
        if (home == null || "".equals(home)) {
            Properties p = System.getProperties();
            for (Object string : p.keySet()) {
                System.out.println(string + "=" + p.get(string));
            }
            System.out.println("Please set system variable " + CLI_HOME_VARIABLE);
            return;
        }
        File homeDir = getHomeDir();
        if (!homeDir.exists()) {
            System.out.println(CLI_HOME_VARIABLE + " home directory [" + homeDir.getAbsolutePath()
                    + "]does not exist.");
            return;
        }
        loadJars();
        try {
            Class<?> cliMain = Thread.currentThread().getContextClassLoader().loadClass(MAIN_CLASS);
            Constructor<?> c = cliMain.getConstructor(String[].class);
            c.setAccessible(true);
            Object o = c.newInstance((Object) args);
            Method m = o.getClass().getMethod("run");
            m.setAccessible(true);
            m.invoke(o);
        } catch (Exception e) {
            // cant do much about this.
            throw new RuntimeException(e);
        }

    }

    public final static File getHomeDir() {
        try {
            return new File(System.getProperty(CLI_HOME_VARIABLE)).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final static File getLibDir() {
        try {
            return new File(getHomeDir(), LIB_DIR).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void loadJars() {
        try {
            File root = getHomeDir();
            File boot = new File(root, BOOT_DIR).getCanonicalFile();
            loadJarsInThreadCLChild(boot);
            File lib = new File(root, LIB_DIR).getCanonicalFile();
            loadJarsInThreadCLChild(lib);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadJarsInThreadCLChild(File dir) throws IOException {
        File[] jars = listJars(dir);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URLClassLoader jarExtensions = createClassLoaderFrom(jars, cl);
        Thread.currentThread().setContextClassLoader(jarExtensions);
    }

    private static File[] listJars(File dir) {
        File[] listFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().toLowerCase().endsWith(".jar"));
            }
        });
        return listFiles;
    }

    private static URLClassLoader createClassLoaderFrom(File[] jars, ClassLoader parent) {
        URL[] urls = toURLs(jars);
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        return classLoader;
    }

    private static URL[] toURLs(File[] files) {
        if (files == null) {
            return new URL[0];
        }
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                urls[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return urls;
    }
}

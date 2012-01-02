package org.deephacks.tools4j.support.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileUtils {

    public static Set<File> findFiles(File rootdir, FileFilter filter) {
        Set<File> matches = new HashSet<File>();
        Set<File> dirs = new HashSet<File>();
        listRecursively(rootdir, 0, dirs);
        for (File dir : dirs) {
            File[] files = dir.listFiles(filter);
            matches.addAll(Arrays.asList(files));
        }
        return matches;
    }

    public static Set<File> findFiles(File rootdir, FilenameFilter filter) {
        Set<File> matches = new HashSet<File>();
        Set<File> dirs = new HashSet<File>();
        listRecursively(rootdir, 0, dirs);
        for (File dir : dirs) {
            File[] files = dir.listFiles(filter);
            matches.addAll(Arrays.asList(files));
        }
        return matches;
    }

    private static void listRecursively(File fdir, int depth, Set<File> dirs) {
        if (fdir.isDirectory() && (depth < 5)) {
            dirs.add(fdir);
            for (File f : fdir.listFiles()) { // Go over each file/subdirectory.
                listRecursively(f, depth + 1, dirs);
            }
        }
    }

    public static String[] read(InputStream inputStream) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            List<String> buffer = new ArrayList<String>();
            String line = null;
            while ((line = in.readLine()) != null) {
                buffer.add(line);
            }
            return buffer.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static URL[] toURLs(File[] files) {
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

    public static void writeFile(String line, File file) {
        writeFile(Arrays.asList(new String[] { line }), file);
    }

    public static void writeFile(String[] lines, File file) {
        writeFile(Arrays.asList(lines), file);
    }

    public static void writeFile(List<String> lines, File file) {
        try {
            File parent = file.getParentFile();
            if ((parent != null) && parent.isDirectory() && !parent.exists()) {
                parent.mkdirs();
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unxpected exception when trying to create parent folders for file ["
                            + file.getAbsolutePath() + "].");
        }

        try {
            writeFile(lines, new FileOutputStream(file));
        } catch (IOException e) {
            throw new IllegalArgumentException("File [" + file.getAbsolutePath()
                    + "] cant write to file.", e);
        }

    }

    public static void writeFile(List<String> lines, OutputStream stream) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(stream));
            for (String line : lines) {
                bw.write(line + "\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(bw);
        }
    }

    public static void close(Closeable closable) {
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

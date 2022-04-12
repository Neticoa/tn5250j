/**
 *
 */
package com.metrixware.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JfxSwtClassLoader extends URLClassLoader {

    private static final String JAR_NAME = "jfxswt.jar";

    public JfxSwtClassLoader(final ClassLoader parent) throws IOException {
        super(new URL[] {searchJfxSwtJar()}, parent);
    }

    /**
     * @return URL of jfxswt.jar file
     * @throws IOException
     */
    private static URL searchJfxSwtJar() throws IOException {
        final File javaHome = new File(System.getProperty("java.home"));
        final File file = searchJfxSwtJar(javaHome);

        if (file != null) {
            return file.toURI().toURL();
        }

        throw new FileNotFoundException(JAR_NAME);
    }

    /**
     * @param folder parent folder.
     * @return jar file or null.
     */
    private static File searchJfxSwtJar(final File folder) {
        for (final File f : folder.listFiles()) {
            if (f.isFile() && f.getName().equals(JAR_NAME)) {
                return f;
            }
            if (f.isDirectory()) {
                final File found = searchJfxSwtJar(f);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public static void main(final String[] args) throws IOException {
        System.out.println(searchJfxSwtJar());
    }
}

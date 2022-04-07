// InterpreterDriverManager.java
package org.tn5250j.scripting;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.SessionGui;

/**
 * Class for managing interpreter drivers.
 * This manager is responsible for keeping track of loaded
 * driver. Interpreter drivers are required to register instance
 * of themselves with this manager when they are loaded.
 */
public class InterpreterDriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(InterpreterDriverManager.class);

    private static Map<String, InterpreterDriver> _extensionDriverMap = new HashMap<String, InterpreterDriver>();
    private static Map<String, InterpreterDriver> _languageDriverMap = new HashMap<String, InterpreterDriver>();

    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * Private constructor
     * There is no need for instantiating this class as all methods
     * are private. This private constructor is to disallow creating
     * instances of this class.
     */
    private InterpreterDriverManager() {
    }

    /**
     * Register a driver.
     * Interpreter drivers call this method when they are loaded.
     *
     * @param driver the driver to be registered
     */
    public static void registerDriver(final InterpreterDriver driver) {
        final String[] extensions = driver.getSupportedExtensions();
        for (int size = extensions.length, i = 0; i < size; i++) {
            _extensionDriverMap.put(extensions[i], driver);
        }
        final String[] languages = driver.getSupportedLanguages();
        for (int size = languages.length, i = 0; i < size; i++) {
            _languageDriverMap.put(languages[i], driver);
        }
    }

    /**
     * Execute a script string
     * Execute the string supplied according to the langauge specified
     *
     * @param script   script to be executed
     * @param language language for interpreting the script string
     */
    public static void executeScript(final SessionGui session, final String script, final String language)
            throws InterpreterDriver.InterpreterException {
        final InterpreterDriver driver
                = _languageDriverMap.get(language);
        if (driver == null) {
            LOG.warn("No driver installed to handle language "
                    + language);
            return;
        }

        driver.executeScript(session, script);
    }

    /**
     * Exceute a script file.
     * The interpreter driver supporting the language for this file
     * is deduced from file name extension
     *
     * @param scriptFile file name containing script
     */
    public static void executeScriptFile(final SessionGui session, final String scriptFile)
            throws InterpreterDriver.InterpreterException {
        final String extension
                = scriptFile.substring(scriptFile
                .lastIndexOf(EXTENSION_SEPARATOR) + 1);
        final InterpreterDriver driver
                = _extensionDriverMap.get(extension);
        if (driver == null) {
            LOG.warn("No driver installed to handle extension "
                    + extension);
            return;
        }
        driver.executeScriptFile(session, scriptFile);
    }

    /**
     * Exceute a script file.
     * The interpreter driver supporting the language for this file
     * is deduced from file name extension
     *
     * @param scriptFile file name containing script
     */
    public static void executeScriptFile(final String scriptFile)
            throws InterpreterDriver.InterpreterException {
        final String extension
                = scriptFile.substring(scriptFile
                .lastIndexOf(EXTENSION_SEPARATOR) + 1);
        final InterpreterDriver driver
                = _extensionDriverMap.get(extension);
        if (driver == null) {
            LOG.warn("No driver installed to handle extension "
                    + extension);
            return;
        }
        driver.executeScriptFile(scriptFile);
    }


    /**
     * Check if there is a driver that supports the language.
     *
     * @param scriptFile file name containing script
     */
    public static boolean isScriptSupported(final String scriptFile) {
        final String extension
                = scriptFile.substring(scriptFile
                .lastIndexOf(EXTENSION_SEPARATOR) + 1);

        return _extensionDriverMap.containsKey(extension);
    }

}

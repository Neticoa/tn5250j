package com.metrixware.eclipse;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
class JavaFxClassLoaderHook extends ClassLoaderHook {
    private final ClassLoader systemLoader;

    /**
     * Default constructor.
     */
    public JavaFxClassLoaderHook() {
        systemLoader = ClassLoader.getSystemClassLoader();
    }

    /* (non-Javadoc)
     * @see org.eclipse.osgi.internal.hookregistry.ClassLoaderHook#preFindClass(java.lang.String, org.eclipse.osgi.internal.loader.ModuleClassLoader)
     */
    @Override
    public Class<?> preFindClass(final String name, final ModuleClassLoader classLoader) throws ClassNotFoundException {
        final Class<?> result = super.preFindClass(name, classLoader);
        if (result == null && (name.contains("javafx.") || name.startsWith("com.sun."))) {
            try {
                return systemLoader.loadClass(name);
            } catch (final Exception e) {
            }
        }
        return result;
    }
}

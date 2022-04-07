package com.metrixware.eclipse;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.tn5250j.tools.LangTool;

import com.metrixware.log4j.BundleAppender;
import com.metrixware.tn5250.session.SessionManager;

import javafx.embed.swt.FXCanvas;

public class Activator implements BundleActivator {

    public static final String BUNDLE_SYMBOLIC_NAME = "om.metrixware.emulator.tn5250.plugin"; //$NON-NLS-1$

    private static Activator ACTIVATOR;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private SessionManager sessionManager;

    private ServiceReference<EnvironmentInfo> configRef;
    private BundleContext context;

    @Override
    public void start(final BundleContext context) throws Exception {
        ACTIVATOR = this;

        this.context = context;
        configRef = context.getServiceReference(EnvironmentInfo.class);

        BundleAppender.setBundle(context.getBundle());
        LangTool.init();

        //init session manager olny after log4j initialization because the class contains loggings
        sessionManager = new SessionManager();
    }

    /**
     * This is hack method for adding of classloader hook for support of loading JavaFX classes
     */
    public void initializeFxClassLoaderHook() {
        if (initialized.getAndSet(true)) {
            return;
        }

        final EquinoxConfiguration equinoxConfig = (EquinoxConfiguration) context.getService(configRef);

        final HookRegistry hookRegistry = equinoxConfig.getHookRegistry();
        setInitialized(hookRegistry, false);

        try {
            hookRegistry.addClassLoaderHook(new JavaFxClassLoaderHook());
        } finally {
            setInitialized(hookRegistry, false);
        }

        try {
            System.out.println(context.getBundle().loadClass("javafx.scene.paint.Paint"));
            System.out.println("Try static initialization for " + FXCanvas.class.getName());
        } catch (final Exception e) {
            throw new RuntimeException(Messages.ErrorTitleFailedToInitializeFx);
        }
    }

    private void setInitialized(final HookRegistry hookRegistry, final boolean b) {
        try {
            final Field f = HookRegistry.class.getDeclaredField("initialized");
            f.setAccessible(true);
            f.set(hookRegistry, b);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Please not use this method in activator on bundle activation because this method is called from classloader
     * and initiates the concurrent modification exception of hooks list.
     * This method should be used i.e. in constructor of UI actions wizards, menu actions, edidtor openings, ...
     */
    public static void initializeFx() {
        getInstance().initializeFxClassLoaderHook();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        context.ungetService(configRef);
        sessionManager.stop();
    }

    public static Activator getInstance() {
        return ACTIVATOR;
    }

    /**
     * @return session manager.
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}

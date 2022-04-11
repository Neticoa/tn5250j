package com.metrixware.eclipse;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.tn5250j.tools.LangTool;

import com.metrixware.log4j.BundleAppender;
import com.metrixware.tn5250.session.SessionManager;

public class Activator implements BundleActivator {

    public static final String BUNDLE_SYMBOLIC_NAME = "com.metrixware.emulator.tn5250.plugin"; //$NON-NLS-1$

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

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.connectdialog.ConnectionDialogController;
import org.tn5250j.event.BootEvent;
import org.tn5250j.event.BootListener;
import org.tn5250j.event.EmulatorActionEvent;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.Tn5250jController;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.framework.common.Sessions;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.gui.TN5250jSplashScreen;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.stage.Screen;

public class My5250 implements BootListener, SessionListener, EmulatorActionListener {

    private static final Logger log = LoggerFactory.getLogger(My5250.class);
    private static final String PARAM_START_SESSION = "-s";

    private Gui5250Frame frame1;
    private String[] sessionArgs = null;
    private static Map<String, String> sessions = new ConcurrentHashMap<>();
    private static BootStrapper strapper = null;
    private SessionManager manager;
    private static List<Gui5250Frame> frames;
    private final TN5250jSplashScreen splash;
    private int step;
    private StringBuilder viewNamesForNextStartBuilder = null;

    My5250(final TN5250jSplashScreen splash) {
        this.splash = splash;

        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);

        loadSessions();
        splash.updateProgress(++step);

        initJarPaths();

        initScripting();

        // sets the starting frame type.  At this time there are tabs which is
        //    default and Multiple Document Interface.
//		startFrameType();

        frames = new ArrayList<Gui5250Frame>();

        newView();

        setDefaultLocale();
        manager = SessionManager.instance();
        splash.updateProgress(++step);
        Tn5250jController.getCurrent();
    }

    /**
     * Check if there are any other instances of tn5250j running
     */
    static private boolean checkBootStrapper(final String[] args) {

        try {
            final Socket boot = new Socket("localhost", 3036);
            try {
                final PrintWriter out = new PrintWriter(boot.getOutputStream(), true);

                // parse args into a string to send to the other instance of
                //    tn5250j
                String opts = null;
                for (int x = 0; x < args.length; x++) {
                    if (opts != null)
                        opts += args[x] + " ";
                    else
                        opts = args[x] + " ";
                }
                out.println(opts);
                out.flush();
            } finally {
                boot.close();
            }

            return true;

        } catch (final IOException e) {
            // TODO: Should be logged @ DEBUG level
            //         System.err.println("No other instances of tn5250j running.");
        }

        return false;
    }

    @Override
    public void bootOptionsReceived(final BootEvent bootEvent) {
        log.info(" boot options received " + bootEvent.getNewSessionOptions());

        // reload setting, to ensure correct bootstraps
        ConfigureFactory.getInstance().reloadSettings();

        // If the options are not equal to the string 'null' then we have
        //    boot options
        if (!bootEvent.getNewSessionOptions().equals("null")) {
            // check if a session parameter is specified on the command line
            final String[] args = new String[TN5250jConstants.NUM_PARMS];
            parseArgs(bootEvent.getNewSessionOptions(), args);


            if (isSpecified("-s", args)) {

                final String sd = getParm("-s", args);
                if (sessions.containsKey(sd)) {
                    parseArgs(sessions.get(sd), args);
                    final String[] args2 = args;
                    final String sd2 = sd;
                    Platform.runLater(() -> newSession(sd2, args2));
                }
            } else {

                if (args[0].startsWith("-")) {
                    Platform.runLater(() -> startNewSession());
                } else {
                    final String[] args2 = args;
                    final String sd2 = args[0];
                    Platform.runLater(() -> newSession(sd2, args2));
                }
            }
        } else {
            Platform.runLater(() -> startNewSession());
        }
    }

    static public void main(String[] args) {
        SwingToFxUtils.initFx();

        final TN5250jSplashScreen splash = UiUtils.callInFxAndWait(() -> {
            final TN5250jSplashScreen s = new TN5250jSplashScreen("tn5250jSplash.jpg");
            s.setSteps(5);
            s.setVisible(true);
            return s;
        });

        if (!isSpecified("-nc", args)) {

            if (!checkBootStrapper(args)) {

                // if we did not find a running instance and the -d options is
                //    specified start up the bootstrap daemon to allow checking
                //    for running instances
                if (isSpecified("-d", args)) {
                    strapper = new BootStrapper();

                    strapper.start();
                }
            } else {

                System.exit(0);
            }
        }

        final My5250 m = new My5250(splash);

        if (strapper != null)
            strapper.addBootListener(m);

        if (args.length > 0) {

            if (isSpecified("-width", args) ||
                    isSpecified("-height", args)) {
                double width = m.frame1.getWidth();
                double height = m.frame1.getHeight();

                if (isSpecified("-width", args)) {
                    width = Integer.parseInt(My5250.getParm("-width", args));
                }
                if (isSpecified("-height", args)) {
                    height = Integer.parseInt(My5250.getParm("-height", args));
                }

                m.frame1.setSize(width, height);
                m.frame1.centerStage();
            }

            /**
             * @todo this crap needs to be rewritten it is a mess
             */
            if (args[0].startsWith("-")) {

                // check if a session parameter is specified on the command line
                if (isSpecified("-s", args)) {

                    final String sd = getParm("-s", args);
                    if (sessions.containsKey(sd)) {
                        sessions.put("emul.default", sd);
                    } else {
                        args = null;
                    }

                }

                // check if a locale parameter is specified on the command line
                if (isSpecified("-L", args)) {
                    Locale.setDefault(parseLocal(getParm("-L", args)));
                }
            }
        }
        LangTool.init();

        List<String> lastViewNames = new ArrayList<String>();
        lastViewNames.addAll(loadLastSessionViewNames());
        lastViewNames.addAll(loadLastSessionViewNamesFrom(args));
        lastViewNames = filterExistingViewNames(lastViewNames);

        if (lastViewNames.size() > 0) {
            insertDefaultSessionIfConfigured(lastViewNames);
            startSessionsFromList(m, lastViewNames);
            if (sessions.containsKey("emul.showConnectDialog")) {
                m.openConnectSessionDialogAndStartSelectedSession();
            }
        } else {
            m.startNewSession();
        }

    }

    private static void startSessionsFromList(final My5250 m, final List<String> lastViewNames) {
        for (int i = 0; i < lastViewNames.size(); i++) {
            final String viewName = lastViewNames.get(i);
            if (!m.frame1.isVisible()) {
                m.splash.updateProgress(++m.step);
                m.splash.setVisible(false);
                m.frame1.setVisible(true);
                m.frame1.setCursor(Cursor.DEFAULT);
            }

            m.sessionArgs = new String[TN5250jConstants.NUM_PARMS];
            My5250.parseArgs(sessions.get(viewName), m.sessionArgs);
            m.newSession(viewName, m.sessionArgs);
        }
    }

    private static void insertDefaultSessionIfConfigured(final List<String> lastViewNames) {
        if (getDefaultSession() != null && !lastViewNames.contains(getDefaultSession())) {
            lastViewNames.add(0, getDefaultSession());
        }
    }

    static List<String> loadLastSessionViewNamesFrom(final String[] commandLineArgs) {
        final List<String> sessionNames = new ArrayList<String>();
        boolean foundRightParam = false;
        for (final String arg : commandLineArgs) {
            if (foundRightParam && !PARAM_START_SESSION.equals(arg)) {
                sessionNames.add(arg);
            }
            foundRightParam = PARAM_START_SESSION.equals(arg);
        }
        return sessionNames;
    }

    static List<String> loadLastSessionViewNames() {
        final List<String> sessionNames = new ArrayList<String>();
        if (sessions.containsKey("emul.startLastView")) {
            final String emulview = sessions.getOrDefault("emul.view", "");
            int idxstart = 0;
            int idxend = emulview.indexOf(PARAM_START_SESSION, idxstart);
            for (; idxend > -1; idxend = emulview.indexOf(PARAM_START_SESSION, idxstart)) {
                final String sessname = emulview.substring(idxstart, idxend).trim();
                if (sessname.length() > 0) {
                    sessionNames.add(sessname);
                }
                idxstart = idxend + PARAM_START_SESSION.length();
            }
            if (idxstart + PARAM_START_SESSION.length() < emulview.length()) {
                final String sessname = emulview.substring(idxstart + PARAM_START_SESSION.length() - 1).trim();
                if (sessname.length() > 0) {
                    sessionNames.add(sessname);
                }
            }
        }
        return sessionNames;
    }

    static List<String> filterExistingViewNames(final List<String> lastViewNames) {
        final List<String> result = new ArrayList<String>();
        for (final String viewName : lastViewNames) {
            if (sessions.containsKey(viewName)) {
                result.add(viewName);
            }
        }
        return result;
    }

    private static boolean containsNotOnlyNullValues(final String[] stringArray) {
        if (stringArray != null) {
            for (final String s : stringArray) {
                if (s != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setDefaultLocale() {
        if (sessions.containsKey("emul.locale")) {
            Locale.setDefault(parseLocal(sessions.get("emul.locale")));
        }
    }

    static private String getParm(final String parm, final String[] args) {

        for (int x = 0; x < args.length; x++) {

            if (args[x].equals(parm))
                return args[x + 1];

        }
        return null;
    }

    private static boolean isSpecified(final String parm, final String[] args) {

        if (args == null)
            return false;

        for (int x = 0; x < args.length; x++) {

            if (args[x] != null && args[x].equals(parm))
                return true;

        }
        return false;
    }

    private static String getDefaultSession() {
        final String defaultSession = sessions.get("emul.default");
        if (defaultSession != null && !defaultSession.trim().isEmpty()) {
            return defaultSession;
        }
        return null;
    }

    private void startNewSession() {

        String sel = "";

        if (containsNotOnlyNullValues(sessionArgs) && !sessionArgs[0].startsWith("-")) {
            sel = sessionArgs[0];
        } else {
            sel = getDefaultSession();
        }

        final Sessions sess = manager.getSessions();

        if (sel != null && sess.getCount() == 0 && sessions.containsKey(sel)) {
            sessionArgs = new String[TN5250jConstants.NUM_PARMS];
            parseArgs(sessions.get(sel), sessionArgs);
        }

        if (sessionArgs == null || sess.getCount() > 0 || sessions.containsKey("emul.showConnectDialog")) {
            openConnectSessionDialogAndStartSelectedSession();
        } else {
            newSession(sel, sessionArgs);
        }
    }


    private void openConnectSessionDialogAndStartSelectedSession() {
        final String sel = UiUtils.callInFxAndWait(this:: openConnectSessionDialog);
        final Sessions sess = manager.getSessions();
        if (sel != null) {
            final String selArgs = sessions.get(sel);
            sessionArgs = new String[TN5250jConstants.NUM_PARMS];
            parseArgs(selArgs, sessionArgs);

            newSession(sel, sessionArgs);
        } else {
            if (sess.getCount() == 0)
                System.exit(0);
        }
    }

    private void startDuplicateSession(SessionGui ses) {

        loadSessions();
        if (ses == null) {
            final Sessions sess = manager.getSessions();
            for (int x = 0; x < sess.getCount(); x++) {

                if ((sess.item(x).getGUI()).isVisible()) {

                    ses = sess.item(x).getGUI();
                    break;
                }
            }
        }

        final String selArgs = sessions.get(ses.getSessionName());
        sessionArgs = new String[TN5250jConstants.NUM_PARMS];
        parseArgs(selArgs, sessionArgs);

        newSession(ses.getSessionName(), sessionArgs);
    }

    private String openConnectSessionDialog() {

        splash.setVisible(false);

        final AtomicReference<ConnectionDialogController> controller = new AtomicReference<>();
        UiUtils.showDialog(frame1.getWindow(), "/fxml/ConnectionDialog.fxml", LangTool.getString("ss.title"),
                c -> controller.set((ConnectionDialogController) c));

        // load the new session information from the session property file
        loadSessions();
        return controller.get().getConnectKey();
    }

    private synchronized void newSession(final String sel, final String[] args) {

        final Map<String, String> sesProps = new ConcurrentHashMap<>();

        String propFileName = null;
        final String session = args[0];

        // Start loading properties
        sesProps.put(TN5250jConstants.SESSION_HOST, session);

        if (isSpecified("-e", args))
            sesProps.put(TN5250jConstants.SESSION_TN_ENHANCED, "1");

        if (isSpecified("-p", args)) {
            sesProps.put(TN5250jConstants.SESSION_HOST_PORT, getParm("-p", args));
        }

        if (isSpecified("-f", args))
            propFileName = getParm("-f", args);

        if (isSpecified("-cp", args))
            sesProps.put(TN5250jConstants.SESSION_CODE_PAGE, getParm("-cp", args));

        if (isSpecified("-gui", args))
            sesProps.put(TN5250jConstants.SESSION_USE_GUI, "1");

        if (isSpecified("-t", args))
            sesProps.put(TN5250jConstants.SESSION_TERM_NAME_SYSTEM, "1");

        if (isSpecified("-132", args))
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_27X132_STR);
        else
            sesProps.put(TN5250jConstants.SESSION_SCREEN_SIZE, TN5250jConstants.SCREEN_SIZE_24X80_STR);

        // are we to use a socks proxy
        if (isSpecified("-usp", args)) {

            // socks proxy host argument
            if (isSpecified("-sph", args)) {
                sesProps.put(TN5250jConstants.SESSION_PROXY_HOST, getParm("-sph", args));
            }

            // socks proxy port argument
            if (isSpecified("-spp", args))
                sesProps.put(TN5250jConstants.SESSION_PROXY_PORT, getParm("-spp", args));
        }

        // are we to use a ssl and if we are what type
        if (isSpecified("-sslType", args)) {

            sesProps.put(TN5250jConstants.SSL_TYPE, getParm("-sslType", args));
        }


        // check if device name is specified
        if (isSpecified("-dn=hostname", args)) {
            String dnParam;

            // use IP address as device name
            try {
                dnParam = InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException uhe) {
                dnParam = "UNKNOWN_HOST";
            }

            sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME, dnParam);
        } else if (isSpecified("-dn", args)) {

            sesProps.put(TN5250jConstants.SESSION_DEVICE_NAME, getParm("-dn", args));
        }

        if (isSpecified("-hb", args))
            sesProps.put(TN5250jConstants.SESSION_HEART_BEAT, "1");

        final int sessionCount = manager.getSessions().getCount();

        final Session5250 s2 = manager.openSession(sesProps, propFileName, sel);

        final SessionGui gui = UiUtils.callInFxAndWait(() -> {
            final SessionGui s = SessionGuiFactory.createGui(s2);

            if (!frame1.isVisible()) {
                splash.updateProgress(++step);

                // Here we check if this is the first session created in the system.
                //  We have to create a frame on initialization for use in other scenarios
                //  so if this is the first session being added in the system then we
                //  use the frame that is created and skip the part of creating a new
                //  view which would increment the count and leave us with an unused
                //  frame.
                if (isSpecified("-noembed", args) && sessionCount > 0) {
                    newView();
                }
                splash.setVisible(false);
                frame1.setVisible(true);
                frame1.setCursor(Cursor.DEFAULT);
            } else {
                if (isSpecified("-noembed", args)) {
                    splash.updateProgress(++step);
                    newView();
                    splash.setVisible(false);
                    frame1.setVisible(true);
                    frame1.setCursor(Cursor.DEFAULT);
                }
            }

            if (isSpecified("-t", args))
                frame1.addSessionView(sel, s);
            else
                frame1.addSessionView(session, s);
            return s;
        });

        gui.connect();
        gui.addEmulatorActionListener(this);
    }

    private void newView() {
        Platform.runLater(() -> {
            frame1 = new Gui5250Frame(this);
            frame1.setCursor(Cursor.WAIT);

            if (sessions.containsKey("emul.frame" + frame1.getFrameSequence())) {
                final String location = sessions.get("emul.frame" + frame1.getFrameSequence());
                //         System.out.println(location + " seq > " + frame1.getFrameSequence() );
                restoreFrame(frame1, location);
            } else {

                // we will now to default the frame size to take over the whole screen
                //    this is per unanimous vote of the user base
                final Rectangle2D bounds = Screen.getPrimary().getBounds();

                double width = bounds.getWidth();
                double height = bounds.getHeight();

                if (sessions.containsKey("emul.width"))
                    width = Integer.parseInt(sessions.get("emul.width"));
                if (sessions.containsKey("emul.height"))
                    height = Integer.parseInt(sessions.get("emul.height"));

                frame1.setSize(width, height);
                frame1.centerStage();
            }

            frames.add(frame1);
        });
    }

    private void restoreFrame(final Gui5250Frame frame, final String location) {

        final StringTokenizer tokenizer = new StringTokenizer(location, ",");
        final int x = Integer.parseInt(tokenizer.nextToken());
        final int y = Integer.parseInt(tokenizer.nextToken());
        final int width = Integer.parseInt(tokenizer.nextToken());
        final int height = Integer.parseInt(tokenizer.nextToken());

        frame.setLocation(x, y);
        frame.setSize(width, height);
    }

    /**
     * @param view
     */
    protected void closingDown(final Gui5250Frame view) {

        final Sessions sess = manager.getSessions();

        if (log.isDebugEnabled()) {
            log.debug("number of active sessions we have " + sess.getCount());
        }

        if (viewNamesForNextStartBuilder == null) {
            // preserve sessions for next boot
            viewNamesForNextStartBuilder = new StringBuilder();
        }
        while (view.getSessionViewCount() > 0) {
            final SessionGui sesspanel = view.getSessionAt(0);
            viewNamesForNextStartBuilder.append("-s ")
                    .append(sesspanel.getSessionName())
                    .append(" ");
            closeSessionInternal(sesspanel);
        }

        sessions.put("emul.frame" + view.getFrameSequence(),
                (int) view.getX() + "," +
                        (int) view.getY() + "," +
                        (int) view.getWidth() + "," +
                        (int) view.getHeight());

        frames.remove(view);
        view.dispose();

        if (log.isDebugEnabled()) {
            log.debug("number of active sessions we have after shutting down " + sess.getCount());
        }

        log.info("view settings " + viewNamesForNextStartBuilder);
        if (sess.getCount() == 0) {

            sessions.put("emul.width", Integer.toString((int) view.getWidth()));
            sessions.put("emul.height", Integer.toString((int) view.getHeight()));

            sessions.put("emul.view", viewNamesForNextStartBuilder.toString());

            // save off the session settings before closing down
            ConfigureFactory.getInstance().saveSettings(ConfigureFactory.SESSIONS,
                    ConfigureFactory.SESSIONS,
                    "------ Defaults --------");
            if (strapper != null) {
                strapper.interrupt();
            }
            System.exit(0);
        }


    }

    /**
     * Really closes the tab/session
     * @param sesspanel
     */
    protected void closeSessionInternal(final SessionGui sesspanel) {
        final Gui5250Frame f = getParentView(sesspanel);
        if (f == null) {
            return;
        }
        final Sessions sessions = manager.getSessions();
        if ((sessions.item(sesspanel.getSession())) != null) {
            f.removeSessionView(sesspanel);
            manager.closeSession(sesspanel);
        }
        if (manager.getSessions().getCount() < 1) {
            closingDown(f);
        }
    }

    private static void parseArgs(final String theStringList, final String[] s) {
        int x = 0;
        final StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
        while (tokenizer.hasMoreTokens()) {
            s[x++] = tokenizer.nextToken();
        }
    }

    private static Locale parseLocal(final String localString) {
        int x = 0;
        final String[] s = {"", "", ""};
        final StringTokenizer tokenizer = new StringTokenizer(localString, "_");
        while (tokenizer.hasMoreTokens()) {
            s[x++] = tokenizer.nextToken();
        }
        return new Locale(s[0], s[1], s[2]);
    }

    private static void loadSessions() {

        sessions = (ConfigureFactory.getInstance()).getProperties(
                ConfigureFactory.SESSIONS);
    }

    @Override
    public void onSessionChanged(final SessionChangeEvent changeEvent) {

        final Session5250 ses5250 = (Session5250) changeEvent.getSource();
        final SessionGui ses = ses5250.getGUI();

        switch (changeEvent.getState()) {
            case TN5250jConstants.STATE_REMOVE:
                closeSessionInternal(ses);
                break;
        }
    }

    @Override
    public void onEmulatorAction(final EmulatorActionEvent actionEvent) {

        final SessionGui ses = (SessionGui) actionEvent.getSource();

        switch (actionEvent.getAction()) {
            case EmulatorActionEvent.CLOSE_SESSION:
                closeSessionInternal(ses);
                break;
            case EmulatorActionEvent.CLOSE_EMULATOR:
                throw new UnsupportedOperationException("Not yet implemented!");
            case EmulatorActionEvent.START_NEW_SESSION:
                startNewSession();
                break;
            case EmulatorActionEvent.START_DUPLICATE:
                startDuplicateSession(ses);
                break;
        }
    }

    private Gui5250Frame getParentView(final SessionGui session) {

        Gui5250Frame f = null;

        for (int x = 0; x < frames.size(); x++) {
            f = frames.get(x);
            if (f.containsSession(session))
                return f;
        }

        return null;

    }

    /**
     * Initializes the scripting environment if the jython interpreter exists
     * in the classpath
     */
    private void initScripting() {

        try {
            Class.forName("org.tn5250j.scripting.JPythonInterpreterDriver");
        } catch (final java.lang.NoClassDefFoundError ncdfe) {
            log.warn("Information Message: Can not find scripting support"
                    + " files, scripting will not be available: "
                    + "Failed to load interpreter drivers " + ncdfe);
        } catch (final Exception ex) {
            log.warn("Information Message: Can not find scripting support"
                    + " files, scripting will not be available: "
                    + "Failed to load interpreter drivers " + ex);
        }

        splash.updateProgress(++step);

    }

    /**
     * Sets the jar path for the available jars.
     * Sets the python.path system variable to make the jython jar available
     * to scripting process.
     *
     * This needs to be rewritten to loop through and obtain all jars in the
     * user directory.  Maybe also additional paths to search.
     */
    private void initJarPaths() {

        String jarClassPaths = System.getProperty("python.path")
                + File.pathSeparator + "jython.jar"
                + File.pathSeparator + "jythonlib.jar"
                + File.pathSeparator + "jt400.jar"
                + File.pathSeparator + "itext.jar";

        if (sessions.containsKey("emul.scriptClassPath")) {
            jarClassPaths += File.pathSeparator + sessions.get("emul.scriptClassPath");
        }

        System.setProperty("python.path", jarClassPaths);

        splash.updateProgress(++step);

    }
}

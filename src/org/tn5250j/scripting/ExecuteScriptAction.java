/**
 * ExecuteScriptAction.java
 * <p>
 * <p>
 * Created: Wed Dec 23 15:22:01 1998
 *
 * @author
 * @version
 */

package org.tn5250j.scripting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tn5250j.SessionGui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ExecuteScriptAction implements EventHandler<ActionEvent> {

    private static final transient Logger LOG = LoggerFactory.getLogger(ExecuteScriptAction.class);

    private String scriptFile;
    private SessionGui ses;
    private final String name;

    public ExecuteScriptAction(final String name, final String scriptFile, final SessionGui session) {
        this.name = name;
        this.scriptFile = scriptFile;
        ses = session;
    }

    public String getName() {
        return name;
    }

    @Override
    public void handle(final ActionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoking " + scriptFile);
        }

        try {
            InterpreterDriverManager.executeScriptFile(ses, scriptFile);
        } catch (final InterpreterDriver.InterpreterException ex) {
            ses.setMacroRunning(false);
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}

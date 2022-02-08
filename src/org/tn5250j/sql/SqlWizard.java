/**
 * Title: SqlWizard.java
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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

package org.tn5250j.sql;

import java.sql.Driver;
import java.sql.DriverManager;

import org.tn5250j.ThirdPartySwing;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

import com.ibm.as400.access.AS400;
import com.ibm.as400.vaccess.SQLConnection;
import com.ibm.as400.vaccess.SQLQueryBuilderPane;
import com.ibm.as400.vaccess.SQLResultSetTablePane;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 */
@ThirdPartySwing
public class SqlWizard extends Stage {

    private static final long serialVersionUID = 1L;
    private SQLConnection connection;
    private AS400 system;
    private SQLQueryBuilderPane queryBuilder;
    private SQLResultSetTablePane tablePane;
    private String name;
    private String password;
    private String host;
    private TextArea queryTextArea;
    private final BorderPane contentPane = new BorderPane();

    public SqlWizard(final String host, final String name, final String password) {

        this.host = host;
        this.name = name;
        this.password = password;

        getIcons().addAll(GUIGraphicsUtils.getApplicationIcons());
        // set title
        setTitle(LangTool.getString("xtfr.wizardTitle"));
        setScene(new Scene(contentPane));

        jbInit();

        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        final double w = bounds.getWidth() / 2;
        final double h = bounds.getHeight() / 2;
        setWidth(w);
        setHeight(h);

        setX((bounds.getWidth() - w) / 2);
        setY((bounds.getHeight() - h) / 2);
    }

    private void jbInit() {
        try {
            // Load the JDBC driver.
            final Driver driver2 = (Driver) Class.forName("com.ibm.as400.access.AS400JDBCDriver").newInstance();
            DriverManager.registerDriver(driver2);

            // Get a connection to the database.  Since we do not
            // provide a user id or password, a prompt will appear.
            connection = new SQLConnection("jdbc:as400://" + host, name, password);

            // Create an SQLQueryBuilderPane
            // object. Assume that "connection"
            // is an SQLConnection object that is
            // created and initialized elsewhere.
            queryBuilder = new SQLQueryBuilderPane(connection);
            queryBuilder.setTableSchemas(new String[]{"*USRLIBL"});

            // Load the data needed for the query
            // builder.
            queryBuilder.load();

            final Button done = new Button(LangTool.getString("xtfr.tableDone"));
            done.setOnAction(e -> fillQueryTextArea());

            final HBox panel = new HBox();
            panel.setAlignment(Pos.CENTER);
            panel.getChildren().add(done);

            final SwingNode queryBuilderNode = new SwingNode();
            queryBuilderNode.setContent(queryBuilder);

            contentPane.setCenter(queryBuilderNode);
            contentPane.setBottom(panel);
        } catch (final Exception cnfe) {
            UiUtils.showError("Error loading AS400 JDBC Driver", "Error");
        }
    }

    private void fillQueryTextArea() {
        queryTextArea.appendText(queryBuilder.getQuery());
        hide();
    }

    public void setQueryTextArea(final TextArea qta) {
        queryTextArea = qta;
    }
}

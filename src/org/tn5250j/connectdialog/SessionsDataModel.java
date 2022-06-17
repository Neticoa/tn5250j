/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.connectdialog;

import java.util.function.Consumer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Simple data model representing rows within the table.
 */
public class SessionsDataModel {
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name");
    private final SimpleStringProperty host = new SimpleStringProperty(this, "host");
    private final SimpleBooleanProperty deflt = new SimpleBooleanProperty(this, "deflt");

    private Consumer<String> defaultStateConsumer;

    public SessionsDataModel(final String name, final String host, final Boolean deflt) {
        this.name.set(name);
        this.host.set(host);
        setDeflt(deflt);

        this.deflt.addListener((src, old, value) -> defltChanged(value));
    }

    public Boolean getDeflt() {
        return deflt.getValue();
    }
    public void setDeflt(final Boolean deflt) {
        this.deflt.set(Boolean.TRUE.equals(deflt));
    }
    public String getName() {
        return name.get();
    }
    public String getHost() {
        return host.get();
    }

    SimpleStringProperty getNameProperty() {
        return name;
    }
    SimpleStringProperty getHostProperty() {
        return host;
    }
    SimpleBooleanProperty getDefltProperty() {
        return deflt;
    }

    public void setDefaultStateConsumer(final Consumer<String> defaultStateConsumer) {
        this.defaultStateConsumer = defaultStateConsumer;
    }

    private void defltChanged(final Boolean value) {
        if (defaultStateConsumer != null && value) {
            defaultStateConsumer.accept(getName());
        }
    }
}

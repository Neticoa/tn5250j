/**
 *
 */
package org.tn5250j.connectdialog;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
 *
 */
public class ConnectEvent extends Event {
    private static final long serialVersionUID = 334870433379304299L;
    public static final EventType<ConnectEvent> TYPE = new EventType<>(ANY);

    public ConnectEvent() {
        super(TYPE);
    }
}

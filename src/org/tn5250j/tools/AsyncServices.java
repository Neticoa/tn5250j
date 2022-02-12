/**
 *
 */
package org.tn5250j.tools;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AsyncServices {
    public static <V> void startTask(final Task<V> task) {
        final Service<V> service = new Service<V>() {
            @Override
            protected Task<V> createTask() {
                return task;
            }
        };
        service.start();
    }
}

/**
 *
 */
package org.tn5250j.tools;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

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
    public static <V> void startTask(final Callable<V> call, final Consumer<V> onSuccess, final Consumer<Throwable> onError) {
        startTask(new Task<V>() {
            @Override
            protected V call() throws Exception {
                return call.call();
            }
            @Override
            protected void succeeded() {
                if (onSuccess != null) {
                    onSuccess.accept(getValue());
                }
            }
            @Override
            protected void failed() {
                if (onError != null) {
                    onError.accept(getException());
                }
            }
        });
    }
}

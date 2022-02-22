/**
 *
 */
package org.tn5250j.gui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FxProxyBuilder {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T buildProxy(final T delegate, final Class<T> iface) {
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return UiUtils.callInFxAndWait(() -> method.invoke(delegate, args));
            }
        };

        final Class[] interfaces = new Class[] {iface};
        return (T) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), interfaces, handler);
    }
}

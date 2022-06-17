/*
 * Copyright(c) 2022 Metrixware Systemobjects SAS.
 */
package org.tn5250j.gui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Vyacheslav Soldatov &lt;vyacheslav.soldatov@inbox.ru&gt;
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

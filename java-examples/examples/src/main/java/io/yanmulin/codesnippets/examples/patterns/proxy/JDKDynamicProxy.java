package io.yanmulin.codesnippets.examples.patterns.proxy;

import java.lang.reflect.*;

public class JDKDynamicProxy implements InvocationHandler {

    private Object object;

    public JDKDynamicProxy(Object object) {
        this.object = object;
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        beforeInvoke(m, args);
        try {
            return m.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Throwable e) {
            throw new RuntimeException("unexpected exception", e);
        } finally {
            afterInvoke(m, args);
        }
    }

    protected static void beforeInvoke(Method m, Object[] args) {
        System.out.println("before invoking " + m.getName());
    }

    protected void afterInvoke(Method m, Object[] args) {
        System.out.println("after invoking " + m.getName());
    }

    public static Object newInstance(Object object) {
        return Proxy.newProxyInstance(
                object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new JDKDynamicProxy(object)
        );
    }

    public static void main(String[] args) {
        Stub.Foo foo = (Stub.Foo) JDKDynamicProxy.newInstance(new Stub.FooImpl());
        System.out.println("returned " + foo.bar());
    }
}

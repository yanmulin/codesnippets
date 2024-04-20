package io.yanmulin.codesnippets.patterns.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class GCLibDynamicProxy implements MethodInterceptor {

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        beforeInvoke(method, args);
        try {
            return methodProxy.invokeSuper(obj, args);
        } finally {
            afterInvoke(method, args);
        }
    }

    protected void beforeInvoke(Method method, Object[] args) {
        System.out.println("before invoking " + method.getName());
    }

    protected void afterInvoke(Method method, Object[] args) {
        System.out.println("after invoking " + method.getName());
    }

    public static Object newInstance(Class<?> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new GCLibDynamicProxy());
        return enhancer.create();
    }

    public static void main(String[] args) {
        Stub.Foo foo = (Stub.Foo) GCLibDynamicProxy.newInstance(Stub.FooImpl.class);
        System.out.println("returned " + foo.bar());
    }

}

package io.yanmulin.codesnippets.examples.gc;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

public class MetaspaceOutOfMemory {
    private static class People {
        public People() {}

        public void print() {
            System.out.println("print a people");
        }
    }

    public void addObjects() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        while (true) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(People.class);
            enhancer.setUseCache(false);
            enhancer.setCallback((MethodInterceptor) (o, m, obs, proxy) -> {
                System.out.println("enhanced object, interceptor invoked");
                return proxy.invokeSuper(o, obs);
            });

            People people = (People) enhancer.create();

            people.print();
            System.out.println("total loaded classes: " + classLoadingMXBean.getTotalLoadedClassCount());
            System.out.println("active classes: " + classLoadingMXBean.getLoadedClassCount());
            System.out.println("unloaded classes: " + classLoadingMXBean.getUnloadedClassCount());
        }
    }

    public static void main(String[] args) {
        new MetaspaceOutOfMemory().addObjects();
    }
}

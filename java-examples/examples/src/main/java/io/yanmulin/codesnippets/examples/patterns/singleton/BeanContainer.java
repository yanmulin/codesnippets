package io.yanmulin.codesnippets.examples.patterns.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanContainer {
    private BeanContainer() {}

    private static Map<String, Object> ioc = new ConcurrentHashMap<>();

    public static Object getBean(String className) {
        synchronized (ioc) {
            if (!ioc.containsKey(className)) {
                Object obj = null;
                try {
                    obj = Class.forName(className).getConstructor(null).newInstance();
                    ioc.put(className, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return obj;
            } else {
                return ioc.get(className);
            }
        }
    }
}

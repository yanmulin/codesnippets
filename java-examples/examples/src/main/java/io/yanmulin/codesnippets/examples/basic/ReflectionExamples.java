package io.yanmulin.codesnippets.examples.basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionExamples {

    public void hello(String name) {
        System.out.println("hello, " + name);
    }

    public void method() {
        try {
            Method method = getClass().getMethod("hello", String.class);
            method.invoke(this, "John");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ReflectionExamples().method();
    }
}

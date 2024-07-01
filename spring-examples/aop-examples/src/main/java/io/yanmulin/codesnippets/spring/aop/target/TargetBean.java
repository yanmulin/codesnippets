package io.yanmulin.codesnippets.spring.aop;

import org.springframework.stereotype.Component;

@Component
public class TargetBean implements ITarget {
    public void printMessage(String message) {
        System.out.println("execute target printing " + message);
    }

    public void throwException(RuntimeException exception) {
        System.out.println("execute target throwing " + exception);
        throw exception;
    }
}

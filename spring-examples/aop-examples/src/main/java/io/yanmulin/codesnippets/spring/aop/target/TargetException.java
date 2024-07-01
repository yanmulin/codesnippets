package io.yanmulin.codesnippets.spring.aop.target;

public class TargetException extends Exception {
    private int code;
    public TargetException(int code, String message) {
        super("error " + code + ": " + message);
    }
}

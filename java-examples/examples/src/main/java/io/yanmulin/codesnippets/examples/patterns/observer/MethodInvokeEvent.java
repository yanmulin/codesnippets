package io.yanmulin.codesnippets.examples.patterns.observer;

import lombok.Getter;
import java.util.EventObject;

public class MethodInvokeEvent extends EventObject {
    @Getter
    String methodName;

    public MethodInvokeEvent(Object source, String methodName) {
        super(source);
        this.methodName = methodName;
    }
}

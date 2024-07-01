package io.yanmulin.codesnippets.examples.patterns.observer;

import java.util.EventListener;

public interface MethodInvokeListener extends EventListener {
    void onMethodBegins(MethodInvokeEvent event);
    void onMethodEnds(MethodInvokeEvent event);
}

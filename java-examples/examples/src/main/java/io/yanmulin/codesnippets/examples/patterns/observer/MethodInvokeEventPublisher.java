package io.yanmulin.codesnippets.examples.patterns.observer;

import java.util.ArrayList;
import java.util.List;

public class MethodInvokeEventPublisher {
    private List<MethodInvokeListener> listeners = new ArrayList<>();

    public void addListener(MethodInvokeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MethodInvokeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void publishMethodInvokeBeginsEvent(String methodName) {
        MethodInvokeEvent event = new MethodInvokeEvent(this, methodName);
        List<MethodInvokeListener> listenersCopy = new ArrayList<>(listeners);
        for (MethodInvokeListener listener: listenersCopy) {
            listener.onMethodBegins(event);
        }
    }

    public void publishMethodInvokeEndsEvent(String methodName) {
        MethodInvokeEvent event = new MethodInvokeEvent(this, methodName);
        List<MethodInvokeListener> listenersCopy = new ArrayList<>(listeners);
        for (MethodInvokeListener listener: listenersCopy) {
            listener.onMethodEnds(event);
        }
    }

    private static void hello() {
        System.out.println("hello");
    }

    public static void main(String[] args) {
        MethodInvokeEventPublisher publisher = new MethodInvokeEventPublisher();
        publisher.addListener(new SimpleMethodInvokeEventListener());

        publisher.publishMethodInvokeBeginsEvent("hello");
        hello();
        publisher.publishMethodInvokeEndsEvent("hello");
    }

}

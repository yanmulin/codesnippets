package io.yanmulin.codesnippets.examples.patterns.observer;

public class SimpleMethodInvokeEventListener implements MethodInvokeListener {
    @Override
    public void onMethodBegins(MethodInvokeEvent event) {
        System.out.println(event.getMethodName() + " begins");
    }

    @Override
    public void onMethodEnds(MethodInvokeEvent event) {
        System.out.println(event.getMethodName() + " ends");
    }
}

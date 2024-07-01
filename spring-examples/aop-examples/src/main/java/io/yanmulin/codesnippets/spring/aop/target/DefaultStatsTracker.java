package io.yanmulin.codesnippets.spring.aop.target;

public class DefaultStatsTracker implements IStatsTracker {
    private int counter = 0;

    @Override
    public void increment() {
        System.out.println("increment stats counter, now is " + (++ counter));
    }
}

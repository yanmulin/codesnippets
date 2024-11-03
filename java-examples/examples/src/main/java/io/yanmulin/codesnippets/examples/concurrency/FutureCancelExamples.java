package io.yanmulin.codesnippets.examples.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FutureCancelExamples {

    private static void printFutureStatus(Future<Void> future, boolean cancel) {
        System.out.println("future cancel result: " + cancel);
        System.out.println("future.isCancelled() -> " + future.isCancelled());
        System.out.println("future.isDone() -> " + future.isCancelled());
        try {
            future.get();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void cancelNotStarted() {
        Runnable r = () -> { throw new RuntimeException("should not run"); };
        Future<Void> future = new FutureTask<>(r, null);
        boolean cancel = future.cancel(true);
        printFutureStatus(future, cancel);
    }

    void interruptCancelRunning() throws InterruptedException {
        CountDownLatch runningLatch = new CountDownLatch(1);
        Runnable r = () -> {
            try {
                runningLatch.countDown();
                SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        FutureTask<Void> task = new FutureTask<>(r, null);
        Thread taskThread = new Thread(task);
        taskThread.start();
        runningLatch.await();
        boolean cancel = task.cancel(true);
        printFutureStatus(task, cancel);
        taskThread.join();
    }

    void nonInterruptCancelRunning() throws InterruptedException {
        CountDownLatch runningLatch = new CountDownLatch(1);
        Runnable r = () -> {
            try {
                runningLatch.countDown();
                SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        FutureTask<Void> task = new FutureTask<>(r, null);
        Thread taskThread = new Thread(task);
        taskThread.start();
        runningLatch.await();
        boolean cancel = task.cancel(false);
        printFutureStatus(task, cancel);
        taskThread.join();
    }

    void cancelCompleted() throws InterruptedException {
        Runnable r = () -> System.out.println("run a task");
        FutureTask<Void> task = new FutureTask<>(r, null);
        Thread taskThread = new Thread(task);
        taskThread.start();
        taskThread.join();
        boolean cancel = task.cancel(true);
        printFutureStatus(task, cancel);
    }

    void cancelCancelled() {
        Runnable r = () -> { throw new RuntimeException("should not run"); };
        Future<Void> future = new FutureTask<>(r, null);
        future.cancel(true);
        if (!future.isCancelled()) {
            throw new IllegalStateException("future should be cancelled");
        }
        boolean cancel = future.cancel(true);
        printFutureStatus(future, cancel);
    }

    public static void main(String[] args) throws InterruptedException {
//        new FutureCancelExamples().cancelNotStarted();
//        new FutureCancelExamples().interruptCancelRunning();
//        new FutureCancelExamples().nonInterruptCancelRunning();
//        new FutureCancelExamples().cancelCompleted();
        new FutureCancelExamples().cancelCancelled();
    }
}

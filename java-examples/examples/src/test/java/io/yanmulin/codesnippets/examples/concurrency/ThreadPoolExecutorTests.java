package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public class ThreadPoolExecutorTests {

    private static final int TERMINATION_WAIT_MILLIS = 1000;

    class TrackingThreadFactory implements ThreadFactory {

        int threadCreated = 0;

        @Override
        public Thread newThread(Runnable r) {
            threadCreated ++;
            return new Thread(r);
        }
    }

    @Test
    public void threadCreation() throws InterruptedException {
        int nThreads = 5;
        int nTasks = nThreads * 3;
        CountDownLatch runLatch = new CountDownLatch(1);
        TrackingThreadFactory threadFactory = new TrackingThreadFactory();
        ExecutorService executor = Executors.newFixedThreadPool(nThreads, threadFactory);
        for (int i=0;i<nTasks;i++) {
            executor.submit(() -> {
                try {
                    runLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
            });
        }

        Assert.assertEquals(nThreads, threadFactory.threadCreated);
        runLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    }
}

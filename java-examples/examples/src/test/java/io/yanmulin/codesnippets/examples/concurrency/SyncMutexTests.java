package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;

public class SyncMutexTests {
    private static int N_THREADS = 100;
    private static int N_INCREMENTS = 1000;
    private static volatile int counter = 0;

    @Test
    public void testConcurrency() throws Exception {
        counter = 0;
        Thread[] threads = new Thread[N_THREADS];
        SyncMutex lock = new SyncMutex();
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS + 1);

        for (int i=0;i<N_THREADS;i++) {
            threads[i] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int k = 0; k< N_INCREMENTS; k++) {
                        lock.lock();
                        counter += 1;
                        lock.unlock();
                    }
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        barrier.await();
        barrier.await();
        Assert.assertEquals(N_INCREMENTS * N_THREADS, counter);

        for (int i=0;i<N_THREADS;i++) {
            threads[i].join();
        }
    }
}

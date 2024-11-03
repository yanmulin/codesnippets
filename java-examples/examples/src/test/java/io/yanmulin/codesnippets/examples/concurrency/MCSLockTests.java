package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;

public class MCSLockTests {

    private static final int N_THREADS = 100;
    private static final int N_ITERATIONS = 1000;
    private static int counter = 0;

    @Before
    public void setup() {
        counter = 0;
    }

    @Test
    public void testConcurrency() throws Exception {
        MCSLock lock = new MCSLock();
        Thread[] threads = new Thread[N_THREADS];
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS+1);

        for (int i = 0; i < N_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int j=0; j<N_ITERATIONS; j++) {
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
        Assert.assertEquals(N_THREADS * N_ITERATIONS, counter);

        for (int i = 0; i < N_THREADS; i++) {
            threads[i].join();
        }
    }
}

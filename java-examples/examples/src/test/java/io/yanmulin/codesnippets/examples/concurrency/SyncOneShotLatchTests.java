package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class SyncOneShotLatchTests {
    private static final int N_THREADS = 10;
    private static final int BLOCKING_DETECT_TIMEOUT =  100;

    @Test
    public void testInitialBlocking() throws InterruptedException {
        SyncOneShotLatch latch = new SyncOneShotLatch();

        Thread t = new Thread(() -> {
            try {
                latch.await();
                Assert.fail("gate should block");
            } catch (InterruptedException e) {}
        });

        t.start();
        Thread.sleep(BLOCKING_DETECT_TIMEOUT);
        t.interrupt();
        t.join(BLOCKING_DETECT_TIMEOUT);
    }

    @Test
    public void testNoWaiting() throws InterruptedException {
        SyncOneShotLatch latch = new SyncOneShotLatch();
        latch.signal();
        latch.await();
    }

    @Test
    public void testSignal() throws Exception {
        SyncOneShotLatch latch = new SyncOneShotLatch();
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS + 1);

        Thread[] threads = new Thread[N_THREADS];
        for (int i=0;i<N_THREADS;i++) {
            threads[i] = new Thread(() -> {
                try {
                    latch.await();
                    barrier.await();
                } catch (Throwable throwable) {
                    Assert.fail("exception: " + throwable.getMessage());
                }
            });
            threads[i].start();
        }

        Thread.sleep(BLOCKING_DETECT_TIMEOUT);
        latch.signal();
        barrier.await(BLOCKING_DETECT_TIMEOUT, TimeUnit.MILLISECONDS);

        for (Thread t: threads) {
            t.join(BLOCKING_DETECT_TIMEOUT);
        }
    }
}

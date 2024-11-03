package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class ReclosableGateTests {

    private static final int BLOCKING_DETECT_TIMEOUT =  100;

    @Test
    public void testInitialBlocking() throws InterruptedException {
        ReclosableGate g = new ReclosableGate();

        Thread t = new Thread(() -> {
            try {
                g.await();
                Assert.fail("gate should block");
            } catch (InterruptedException e) {}
        });

        t.start();
        Thread.sleep(BLOCKING_DETECT_TIMEOUT);
        Assert.assertEquals(1, g.waitingThreads.get());
        t.interrupt();
        t.join(BLOCKING_DETECT_TIMEOUT);
    }

    @Test
    public void testOpen() throws InterruptedException {
        ReclosableGate g = new ReclosableGate();
        CountDownLatch doneLatch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                g.await();
                doneLatch.countDown();
            } catch (Throwable throwable) {
                Assert.fail("exception: " + throwable.getMessage());
            }
        });
        t.start();
        Thread.sleep(BLOCKING_DETECT_TIMEOUT);
        Assert.assertEquals(1, g.waitingThreads.get());
        g.open();
        doneLatch.await(BLOCKING_DETECT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, g.waitingThreads.get());
        t.join(BLOCKING_DETECT_TIMEOUT);
    }

    @Test
    public void testWaitOpenGate() throws InterruptedException {
        ReclosableGate g = new ReclosableGate();
        CountDownLatch doneLatch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                g.await();
                doneLatch.countDown();
            } catch (Throwable throwable) {
                Assert.fail("exception: " + throwable.getMessage());
            }
        });

        g.open();
        t.start();
        doneLatch.await(BLOCKING_DETECT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, g.waitingThreads.get());
        t.join(BLOCKING_DETECT_TIMEOUT);
    }

    @Test
    public void testCloseGate() throws InterruptedException {
        ReclosableGate g = new ReclosableGate();
        g.open();
        g.close();

        CountDownLatch doneLatch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                g.await();
                doneLatch.countDown();
            } catch (Throwable throwable) {
                Assert.fail("exception: " + throwable.getMessage());
            }
        });

        t.start();
        Thread.sleep(BLOCKING_DETECT_TIMEOUT);
        Assert.assertEquals(1, g.waitingThreads.get());
        g.open();
        doneLatch.await(BLOCKING_DETECT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, g.waitingThreads.get());
        t.join(BLOCKING_DETECT_TIMEOUT);
    }

    @Test
    public void testLiveness() throws BrokenBarrierException, InterruptedException {
        final int N_THREADS = 100;
        final int N_GENERATIONS = 10;

        ReclosableGate gate = new ReclosableGate();
        CyclicBarrier barrier = new CyclicBarrier(N_THREADS + 1);

        Thread[] threads = new Thread[N_THREADS];
        for (int i=0;i<N_THREADS;i++) {
            threads[i] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int j=0;j<N_GENERATIONS;j++) {
                        gate.await();
                    }
                    barrier.await();
                } catch (Throwable throwable) {
                    Assert.fail("exception: " + throwable.getMessage());
                }
            });
            threads[i].start();
        }

        barrier.await();
        for (int i=0;i<N_GENERATIONS;i++) {
            Thread.sleep(5);
            gate.open();
            gate.close();
        }
        gate.open();
        barrier.await();

        for (int i=0;i<N_THREADS;i++) {
            threads[i].join(BLOCKING_DETECT_TIMEOUT);
        }

    }
}

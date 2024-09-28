package io.yanmulin.codesnippets.examples.concurrency;

import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledThreadPoolExecutionTests {

    private static final long TERMINATION_WAIT_MILLIS = 10_000;
    ScheduledThreadPoolExecutorImpl pool;

    @Test
    public void testSingleNonRepeating() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(1);
        AtomicInteger runCount = new AtomicInteger();
        Runnable r = () -> runCount.incrementAndGet();
        pool.schedule(r, 0, TimeUnit.MILLISECONDS);
        pool.shutdown();
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, runCount.get());
        Assert.assertEquals(1, pool.getCompletedTasks());
    }

    @Test
    public void testSingleFixedPeriod() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(1);
        CountDownLatch notify = new CountDownLatch(2);
        AtomicInteger runCount = new AtomicInteger(0);
        Runnable r = () -> {
            runCount.incrementAndGet();
            notify.countDown();
        };
        pool.schedule(r, 0, 10, TimeUnit.MILLISECONDS);

        Assert.assertFalse(notify.await(5, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, runCount.get());
        Assert.assertTrue(notify.await(20, TimeUnit.MILLISECONDS));
        Assert.assertEquals(2, runCount.get());

        pool.shutdown();
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSingleFixedDelay() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(1);
        CountDownLatch notify = new CountDownLatch(2);
        AtomicInteger runCount = new AtomicInteger(0);

        Runnable r = () -> {
            try {
                runCount.incrementAndGet();
                notify.countDown();
                Thread.sleep(20);
            } catch (Throwable t) {}
        };
        pool.scheduleWithFixedDelay(r, 0, 10, TimeUnit.MILLISECONDS);

        Assert.assertFalse(notify.await(5, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, runCount.get());
        Assert.assertFalse(notify.await(20, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, runCount.get());
        Assert.assertTrue(notify.await(10, TimeUnit.MILLISECONDS));
        Assert.assertEquals(2, runCount.get());

        pool.shutdown();
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        Assert.assertEquals(2, pool.getCompletedTasks());
    }

    @Test
    public void zeroCorePoolSize() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(0);
        AtomicInteger runCount = new AtomicInteger();
        CountDownLatch notify = new CountDownLatch(2);
        Runnable r = () -> {
            try {
                runCount.incrementAndGet();
                notify.countDown();
            } catch (Throwable t) {}
        };
        pool.schedule(r, 0, 10, TimeUnit.MILLISECONDS);
        Assert.assertTrue(notify.await(20, TimeUnit.MILLISECONDS));
        Assert.assertEquals(2, runCount.get());

        pool.shutdown();
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, pool.getLargestPoolSize());
    }

    @Test
    public void rejectAfterShutdown() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(0);
        pool.shutdown();
        ScheduledFuture<?> task = pool.schedule(() -> Assert.fail(), 0, TimeUnit.MILLISECONDS);
        Assert.assertTrue(task.isCancelled());
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void cancelTask() throws InterruptedException {
        pool = new ScheduledThreadPoolExecutorImpl(1);
        ScheduledFuture<?> task = pool.schedule(() -> Assert.fail(), 10, TimeUnit.MILLISECONDS);
        pool.shutdownNow();
        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        Assert.assertTrue(task.isCancelled());
    }

    @Test
    public void GCRetention() throws InterruptedException {
        int size = 100;
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        List<WeakReference<?>> refs = new ArrayList<>();
        pool = new ScheduledThreadPoolExecutorImpl(1);

        class Task implements Runnable {
            Object x;
            Task() { refs.add(new WeakReference<>(x = new Object(), q)); }
            public void run() { System.out.println(x); }
        }

        List<Future<?>> futures = new ArrayList<>();
        for (int i = size; i>0; i --) {
            futures.add(pool.schedule(new Task(), i + 1, TimeUnit.MINUTES));
        }
        futures.forEach(f -> f.cancel(false));
        futures.clear();

        pool.shutdownNow();

        // trigger GCs to reclaim `size` objects
        for (int j = size; j > 0; j --) {
            if (q.poll() == null) { // if the j-th object is not reclaimed, trigger GC
                for (;;) {
                    System.gc();
                    // if the GC above reclaim an object, go to next object; otherwise, trigger another GC
                    if (q.remove(1000) != null) break;
                    System.out.printf("%d/%d unqueued references remaining\n", j + 1, size);
                }
            }
        }
        Assert.assertNull(q.poll());
        for (WeakReference<?> ref: refs) {
            Assert.assertNull(ref.get());
        }

        pool.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);

    }
}

package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListBlockingQueueTests {

    private static final int LOCKUP_DETECT_TIMEOUT =  100;
    private static final int TERMINATION_WAIT_MILLIS =  5000;
    private static final int N_THREADS =  100;
    private static final int N_TRIALS = 1000;
    private static final int BIG_OBJECT_BYTES = 100 * 1024;

    class BigObject {
        int rand;
        byte[] bytes;

        public BigObject(int rand) {
            this.rand = rand;
            this.bytes = new byte[BIG_OBJECT_BYTES];
        }
    }

    @Test
    public void testBlocking() throws InterruptedException {

        AtomicBoolean interrupted =  new AtomicBoolean(false);
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

        Thread t = new Thread(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        t.start();
        Thread.sleep(LOCKUP_DETECT_TIMEOUT);
        t.interrupt();
        t.join(LOCKUP_DETECT_TIMEOUT);
        Assert.assertFalse(t.isAlive());
        Assert.assertTrue(interrupted.get());
    }

    @Test
    public void testPutTake() throws BrokenBarrierException, InterruptedException {
        AtomicInteger putSum = new AtomicInteger();
        AtomicInteger takeSum = new AtomicInteger();
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        CyclicBarrier barrier = new CyclicBarrier(2 * N_THREADS + 1);

        for (int i = 0; i < N_THREADS; i++) {
            // producer
            executor.submit(() -> {
                try {
                    int seed = hashCode() ^ (int)System.nanoTime();
                    int sum = 0;

                    barrier.await();
                    for (int t = 0; t < N_TRIALS; t ++) {
                        queue.put(seed);
                        sum += seed;
                        seed = nextRand(seed);
                    }
                    putSum.getAndAdd(sum);
                    barrier.await();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });

            // consumer
            executor.submit(() -> {
                try {
                    int sum = 0;
                    barrier.await();
                    for (int t = 0; t < N_TRIALS; t ++) {
                        sum += queue.take();
                    }
                    takeSum.getAndAdd(sum);
                    barrier.await();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
        }

        barrier.await();
        barrier.await();
        Assert.assertEquals(takeSum.get(), putSum.get());

        executor.shutdown();
        executor.awaitTermination(TERMINATION_WAIT_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testMemoryLeak() throws InterruptedException {
        int seed = (int) System.nanoTime();
        ReferenceQueue<BigObject> refQueue = new ReferenceQueue<>();
        List<WeakReference<BigObject>> refs = new ArrayList<>();
        BlockingQueue<BigObject> queue = new LinkedBlockingQueue<>();

        for (int i=0;i<N_TRIALS;i++) {
            BigObject obj = new BigObject(seed);
            queue.put(obj);
            refs.add(new WeakReference<>(obj, refQueue));
            seed = nextRand(seed);
        }

        for (int i=0;i<N_TRIALS;i++) {
            queue.take();
        }

        reclaim(refQueue, refs);

    }

    private int nextRand(int x) {
        x ^= (x << 6);
        x ^= (x >>> 21);
        x ^= (x << 7);
        return x;
    }

    private <T> void reclaim(ReferenceQueue<T> q, List<WeakReference<T>> refs) throws InterruptedException {
        int n = refs.size();
        for (int i=0;i<n;i++) {
            if (q.poll() == null) {
                for (;;) {
                    System.gc();
                    if (q.remove(1000) != null)
                        break;
                }
            }
        }
        Assert.assertNull(q.poll());

        for (int i=0;i<n;i++) {
            Assert.assertNull(refs.get(i).get());
        }
    }
}

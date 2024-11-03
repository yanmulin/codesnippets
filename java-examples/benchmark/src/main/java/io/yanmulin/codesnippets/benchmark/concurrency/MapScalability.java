package io.yanmulin.codesnippets.benchmark.concurrency;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@State(Scope.Benchmark)
public class MapScalability {

    public class WorkTask implements Runnable {
        Random random = new Random(System.currentTimeMillis());
        @Override
        public void run() {
            try {
                goLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            for (int i=0;i<numRequests / numThreads; i ++) {
                long k = random.nextLong() % keySpace;
                int p = random.nextInt(99);
                m.compute(k, (kk, v) -> {
                    if (v == null) {
                        return 0;
                    } else if (p < 2) {
                        return null;
                    } else {
                        return v + 1;
                    }
                });
            }

            doneLatch.countDown();
        }
    }

    long numRequests = 10_0000_000L;
    long keySpace = numRequests / 1000;

    @Param({"ConcurrentHashMap", "synchronizedMap"})
    String type;

    @Param({"1", "4", "8", "16", "32"})
    int numThreads;

    Map<Long, Integer> m;
    Thread[] threads;
    CountDownLatch goLatch;
    CountDownLatch doneLatch;

    @Setup(value = Level.Iteration)
    public void setUp() {

        switch (type) {
            case "ConcurrentHashMap": m = new ConcurrentHashMap<>(); break;
            case "synchronizedMap": m = Collections.synchronizedMap(new HashMap<>()); break;
        }

        threads = new Thread[numThreads];
        goLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(numThreads);
        for (int i=0;i<numThreads;i++) {
            threads[i] = new Thread(new WorkTask());
            threads[i].start();
        }
    }

    @TearDown(value = Level.Iteration)
    public void tearDown() throws InterruptedException {
        for (int i=0;i<numThreads;i++) {
            threads[i].join();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    public void benchmark() throws InterruptedException {
        goLatch.countDown();
        doneLatch.await();
    }
}

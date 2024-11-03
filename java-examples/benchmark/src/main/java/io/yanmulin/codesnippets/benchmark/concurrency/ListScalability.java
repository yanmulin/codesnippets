package io.yanmulin.codesnippets.benchmark.concurrency;

import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@State(Scope.Benchmark)
public class ListScalability {


    abstract class WorkTask implements Runnable {

        abstract protected Integer getFromQueue() throws Exception;
        @Override
        public void run() {
            try {
                goLatch.await();
                for (int i = 0; i< numRequests / numThreads; i++) {
                    if (getFromQueue() < 0) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doneLatch.countDown();
            }
        }
    }

    final int numRequests = 1_000_000_000;

    @Param({"4", "8", "16", "32"})
    volatile int numThreads;

    @Param({"concurrent", "synchronized"})
    String type;

    Thread[] threads;
    CountDownLatch goLatch;
    CountDownLatch doneLatch;
    BlockingQueue<Integer> blockingQueue;
    List<Integer> list;

    @Setup(value = Level.Iteration)
    public void init() {
        blockingQueue = new LinkedBlockingQueue<>();
        list = Collections.synchronizedList(new LinkedList<>());
        goLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(numThreads);

        threads = new Thread[numThreads];
        for (int i=0;i<numThreads;i++) {
            switch (type) {
                case "concurrent":
                    threads[i] = new Thread(new WorkTask() {
                        @Override
                        protected Integer getFromQueue() throws InterruptedException {
                            blockingQueue.put(1);
                            return blockingQueue.take();
                        }
                    });
                    break;
                case "synchronized":
                    threads[i] = new Thread(new WorkTask() {
                        @Override
                        protected Integer getFromQueue() {
                            list.add(1);
                            return list.remove(0);
                        }
                    });
                    break;
            }
            threads[i].start();
        }
    }

    @TearDown(value = Level.Iteration)
    public void tearDown() {
        for (int i=0;i<numThreads;i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        blockingQueue.clear();
        list.clear();
    }

    @Benchmark
    public void benchmark() throws InterruptedException {
        goLatch.countDown();
        doneLatch.await();
    }

}

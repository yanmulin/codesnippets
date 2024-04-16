package io.yanmulin.codesnippets.threading;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThreadPoolExecutorExample {

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_MILLIS = 1000;
    private static final int QUEUE_CAPACITY = 10;
    private static final int THREAD_NUM = 25;
    private static final long THREAD_SLEEP_MILLIS = 2000;
    private final AtomicInteger nextId = new AtomicInteger(0);
    private final CountDownLatch go = new CountDownLatch(1);

    private final ExecutorService executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(QUEUE_CAPACITY),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    private class SubmitRunnable implements Runnable {
        @Override
        public void run() {
            final int id = nextId.incrementAndGet();
            try {
                go.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                log.info("thread {} id {} started", threadName, id);
                try {
                    Thread.sleep(THREAD_SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    log.error("thread {} id {} error", threadName, id, e);
                    throw new RuntimeException(e);
                } finally {
                    log.info("thread {} id {} done", threadName, id);
                }

            });
        }
    }

    private void run() {
        ThreadGroup threadGroup = new ThreadGroup("submitters");
        for (int i=0;i<THREAD_NUM;i++) {
            new Thread(threadGroup, new SubmitRunnable()).start();
        }

        go.countDown();

        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        for (int i=0;i<THREAD_NUM;i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }

    public static void main(String[] args) { new ThreadPoolExecutorExample().run(); }
}

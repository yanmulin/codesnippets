package io.yanmulin.codesnippets.examples.concurrency;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class ThreadPoolExecutorExamples {

    private void submitSleepTask(ThreadPoolExecutor executor, long sleepMillis, int count) {
        for (int i=0;i<count;i++) {
            executor.execute(() -> {
                log.info("thread {}/{} started running",
                        Thread.currentThread().getName(), Thread.currentThread().getId());
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    log.error("sleep interrupted", e);
                } finally {
                    log.info("thread {}/{} stopped running",
                            Thread.currentThread().getName(), Thread.currentThread().getId());
                }
            });
        }
    }

    private void testInitParams() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                4, 16, 2, SECONDS,
                new LinkedBlockingQueue<>(4), new ThreadPoolExecutor.CallerRunsPolicy()
        );

        assert executor.getPoolSize() == 0;
        assert executor.getActiveCount() == 0;

        // run a task, 1 threads
        submitSleepTask(executor, 100, 1);
        assert executor.getPoolSize() == 1;
        assert executor.getActiveCount() == 1;
        assert executor.getQueue().isEmpty();

        // run 5 tasks, 4 threads, 1 pending in queue
        submitSleepTask(executor, 100, 4);
        assert executor.getPoolSize() == 4;
        assert executor.getActiveCount() == 4;
        assert executor.getQueue().size() == 1;

        // run 20 tasks, queue full and scaled up to 16 threads
        submitSleepTask(executor, 100, 15);
        assert executor.getPoolSize() == 16;
        assert executor.getActiveCount() == 16;
        assert executor.getQueue().size() == 4;

        // reject and run in main thread
        submitSleepTask(executor, 100, 1);

        // wait until all done
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("sleep interrupted", e);
        }

        // thread pool scale down
        assert executor.getPoolSize() == 4;
        assert executor.getActiveCount() == 0;
        assert executor.getQueue().isEmpty();

        executor.shutdown();
    }

    class SleepTask implements Callable<Void> {

        private final int id;
        private final long sleepSeconds;

        private final CountDownLatch runningLatch = new CountDownLatch(1);

        SleepTask(int id, long sleepSeconds) {
            this.id = id;
            this.sleepSeconds = sleepSeconds;
        }

        @Override
        public Void call() throws InterruptedException {
            try {
                System.out.println("task " + id + " starts");
                runningLatch.countDown();
                SECONDS.sleep(sleepSeconds);
            } catch (InterruptedException e) {
                System.out.println("task " + id + " exception " + e);
                throw e;
            } finally {
                System.out.println("task " + id + " ended");
            }
            return null;
        }
    }

    class TrackingThread extends Thread {
        public TrackingThread(Runnable r) { super(r); }
        @Override
        public void run() {
            try {
                System.out.println("thread " + this + " started");
                super.run();
            } finally {
                System.out.println("thread " + this + " ended");
            }
        }
    }

    class TrackingThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return new TrackingThread(r);
        }
    }

    public void cancelTask() throws InterruptedException {
        ExecutorService executor = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new TrackingThreadFactory());

        SleepTask t1 = new SleepTask(1, 5);
        Future<Void> f1 = executor.submit(t1);
        executor.submit(new SleepTask(2, 30));
        Future<Void> f3 = executor.submit(new SleepTask(3, 100));
        executor.submit(new SleepTask(4, 30));
        t1.runningLatch.await();
        f1.cancel(true); // cancel running
        f3.cancel(true); // cancel before start
        executor.shutdown();
        executor.awaitTermination(100, SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
//        new ThreadPoolExecutorExamples().testInitParams();
        new ThreadPoolExecutorExamples().cancelTask();
    }
}

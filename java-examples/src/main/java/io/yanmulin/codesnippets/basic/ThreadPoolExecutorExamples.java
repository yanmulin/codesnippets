package io.yanmulin.codesnippets.basic;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
                4, 16, 2, TimeUnit.SECONDS,
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

    public static void main(String[] args) {
        new ThreadPoolExecutorExamples().testInitParams();
    }



}

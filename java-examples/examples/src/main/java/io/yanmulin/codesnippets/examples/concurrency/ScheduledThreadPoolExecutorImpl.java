package io.yanmulin.codesnippets.examples.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScheduledThreadPoolExecutorImpl {

    class ScheduledFutureTask<V> extends FutureTask<V> implements Delayed, ScheduledFuture<V> {

        long time;
        long period; // 0, non-repeating; positive, fixed-period; negative, fixed-delay
        long sequence;

        public ScheduledFutureTask(Runnable runnable, long triggerTime, long period, long sequence) {
            super(runnable, null);
            this.time = triggerTime;
            this.period = period;
            this.sequence = sequence;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        public boolean isPeriodic() {
            return period != 0;
        }

        @Override
        public int compareTo(Delayed o) {
            if (o == this) {
                return 0;
            }

            long diff;
            if (o instanceof ScheduledFutureTask) {
                ScheduledFutureTask task = (ScheduledFutureTask) o;
                diff = time - task.time;
                if (diff == 0) {
                    diff = sequence - task.sequence;
                }
            } else {
                diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            }

            return diff == 0L ? 0 : (diff < 0 ? -1 : 1);
        }

        @Override
        public void run() {
            if (runStateAtLeast(ctl.get(), STOP)) {
                cancel(false);
            } else if (!isPeriodic()) {
                super.run();
            } else if (super.runAndReset()) {
                setNextRunTime();
                reExecutePeriodic(this);
            }

        }

        private void setNextRunTime() {
            if (period > 0) {
                time += period;
            } else if (period < 0) {
                time = triggerTime(-period);
            }
        }

    }

    class Worker implements Runnable {
        Thread t;
        Semaphore semaphore;
        long completedTasks;

        Worker(ThreadFactory threadFactory) {
            t = threadFactory.newThread(this);
            completedTasks = 0;
            semaphore = new Semaphore(0);
        }

        @Override
        public void run() {
            runWorker(this);
        }

        void lock() throws InterruptedException { semaphore.acquire(); }
        void unlock() { semaphore.release(); }
        boolean tryLock() { return semaphore.tryAcquire(); }
    }

    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int COUNT_MASK = -1 >>> COUNT_BITS;
    private static final int RUNNING = -1 << COUNT_BITS;
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    private static final int STOP = 1 << COUNT_BITS;
    private static final int TIDYING = 2 << COUNT_BITS;
    private static final int TERMINATED = 3 << COUNT_BITS;

    int corePoolSize;
    long keepAliveTime;
    int completedTasks;
    int largestPoolSize;
    final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true).build();
    final AtomicInteger ctl = new AtomicInteger(ctlOf(0, RUNNING));
    final AtomicLong sequencer = new AtomicLong(0L);
    final Set<Worker> workers = new HashSet<>();
    final BlockingQueue<ScheduledFutureTask> workQueue = new DelayQueue<>();
    final ReentrantLock mainLock = new ReentrantLock();
    final Condition termination = mainLock.newCondition();

    private int ctlOf(int workerCount, int state) {
        return (workerCount & COUNT_MASK) | state;
    }

    private int workerCountOf(int c) {
        return c & COUNT_MASK;
    }

    private int runStateOf(int c) {
        return c & (~COUNT_MASK);
    }

    private boolean runStateLessThan(int c, int s) {
        return runStateOf(c) < s;
    }

    private boolean runStateAtLeast(int c, int s) {
        return runStateOf(c) >= s;
    }

    private void advanceRunState(int targetState) {
        int c = ctl.get();
        while (!runStateAtLeast(c, targetState)
                && !ctl.compareAndSet(c, ctlOf(workerCountOf(c), targetState))) {
            c = ctl.get();
        }
    }

    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    private int decrementWorkerCount() {
        return ctl.getAndDecrement();
    }

    private boolean isRunning(int c) {
        return runStateOf(c) < SHUTDOWN;
    }

    private long triggerTime(long delayNanos) {
        return System.nanoTime() + delayNanos;
    }

    private Runnable getTask() {
        boolean timedOut = false;
        while (true) {
            int c = ctl.get();
            if (runStateAtLeast(c, STOP) ||
                    (runStateAtLeast(c, SHUTDOWN) && workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);
            boolean timed = wc > corePoolSize ? true : false;

            if (wc > Math.max(1, corePoolSize) && timed && timedOut) {
                if (compareAndDecrementWorkerCount(c)) {
                    return null;
                } else {
                    continue;
                }
            }

            Runnable r;
            timedOut = false;
            try {
                 r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
                 if (r != null) {
                     return r;
                 }
                 timedOut = true;
            } catch (InterruptedException e) {}
        }
    }

    private void runWorker(Worker worker) {
        Thread t = Thread.currentThread();
        Runnable task;
        boolean exitAbruptly = true;

        worker.unlock();
        try {
            while ((task = getTask()) != null) {
                worker.lock();

                if (runStateAtLeast(ctl.get(), STOP) && !t.isInterrupted()) {
                    t.interrupt();
                }

                try {
                    task.run();
                } finally {
                    worker.completedTasks += 1;
                    worker.unlock();
                }
            }
            exitAbruptly = false;
        } catch (InterruptedException ignore) {
        } finally {
            processWorkerExit(worker, exitAbruptly);
        }
    }

    private void processWorkerExit(Worker worker, boolean abruptly) {
        if (abruptly) {
            decrementWorkerCount();
        }
        tryTerminate();

        mainLock.lock();
        try {
            completedTasks += worker.completedTasks;
            workers.remove(worker);
        } finally {
            mainLock.unlock();
        }

        int c = ctl.get();
        int wc = workerCountOf(c);
        if (runStateLessThan(c, STOP)) {
            if (!abruptly && wc > Math.max(1, corePoolSize)) {
                return;
            }
            addWorker();
        }
    }

    private void addWorker() {
        int c;
        while (true) {
            c = ctl.get();
            if (runStateAtLeast(c, STOP) || (runStateAtLeast(c, SHUTDOWN) && workQueue.isEmpty())) {
                return;
            }

            if (workerCountOf(c) > corePoolSize) {
                return;
            }

            if (compareAndIncrementWorkerCount(c)) {
                break;
            }
        }

        boolean workerAdded = false;
        boolean workerStarted = false;
        Worker worker = new Worker(threadFactory);
        Thread thread = worker.t;
        c = ctl.get();

        mainLock.lock();
        try {
            if (isRunning(c) || (runStateLessThan(c, STOP) && !workQueue.isEmpty())) {

                if (thread.getState() != Thread.State.NEW) {
                    throw new IllegalStateException();
                }


                if (workers.add(worker)) {
                    largestPoolSize = Math.max(largestPoolSize, workers.size());
                    workerAdded = true;
                }

            }
        } finally {
            mainLock.unlock();
        }

        if (workerAdded) {
            thread.start();
            workerStarted = true;
        }

        if (!workerStarted) {
            addWorkerFailed(worker);
        }
    }

    private void addWorkerFailed(Worker worker) {
        decrementWorkerCount();
        tryTerminate();

        mainLock.lock();
        try {
            workers.remove(worker);
        } finally {
            mainLock.unlock();
        }
    }

    private void tryTerminate() {
        while (true) {
            int c = ctl.get();
            if (isRunning(c)
                    || runStateAtLeast(c, TIDYING)
                    || (runStateLessThan(c, STOP) && !workQueue.isEmpty())) {
                return;
            }
            if (workerCountOf(c) > 0) {
                return;
            }

            mainLock.lock();
            try {
                if (!ctl.compareAndSet(c, ctlOf(0, TIDYING))) {
                    continue;
                }
                try {
                    terminated();
                } finally {
                    ctl.set(ctlOf(0, TERMINATED));
                    termination.signalAll();
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void interruptIdleWorkers() {
        mainLock.lock();
        try {
            for (Worker worker : workers) {
                if (!worker.t.isInterrupted() && worker.tryLock()) {
                    try {
                        worker.t.interrupt();
                    } finally {
                        worker.unlock();
                    }
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    private List<Runnable> drainQueue() {
        List<Runnable> tasks = new ArrayList<>();
        workQueue.drainTo(tasks);
        if (!workQueue.isEmpty()) { // delay queue cannot be drained
            for (FutureTask task: workQueue.toArray(new FutureTask[0])) {
                workQueue.remove(task);
                task.cancel(false);
                tasks.add(task);
            }
        }

        return tasks;
    }

    private void terminated() {}

    private void delayedExecute(ScheduledFutureTask<?> task) {
        int c = ctl.get();
        if (!isRunning(c) || !workQueue.add(task)) {
            task.cancel(false);
            return;
        }

        int wc = workerCountOf(c);
        if (wc < corePoolSize || wc == 0) {
            addWorker();
        }
    }

    private void reExecutePeriodic(ScheduledFutureTask task) {
        if (isRunning(ctl.get()) && workQueue.add(task)) {
            return;
        }
        task.cancel(false);
    }

    public ScheduledThreadPoolExecutorImpl(int corePoolSize, long keepAliveTime, TimeUnit unit) {
        this.corePoolSize = corePoolSize;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
    }

    public ScheduledThreadPoolExecutorImpl(int corePoolSize) {
        this(corePoolSize, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable task, long initialDelay, TimeUnit unit) {
        ScheduledFutureTask<Void> futureTask = new ScheduledFutureTask<>(task, triggerTime(unit.toNanos(initialDelay)),
                0, sequencer.getAndIncrement());
        delayedExecute(futureTask);
        return futureTask;
    }

    public ScheduledFuture<?> schedule(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduledFutureTask<Void> futureTask = new ScheduledFutureTask<>(task, triggerTime(unit.toNanos(initialDelay)),
                unit.toNanos(period), sequencer.getAndIncrement());
        delayedExecute(futureTask);
        return futureTask;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduledFutureTask<Void> futureTask = new ScheduledFutureTask<>(task, triggerTime(unit.toNanos(initialDelay)),
                -unit.toNanos(period), sequencer.getAndIncrement());
        delayedExecute(futureTask);
        return futureTask;
    }

    public void shutdown() {
        advanceRunState(SHUTDOWN);
        interruptIdleWorkers();
        tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        advanceRunState(STOP);
        interruptIdleWorkers();
        tasks = drainQueue();
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return runStateAtLeast(ctl.get(), SHUTDOWN);
    }

    public boolean isStopped() {
        return runStateAtLeast(ctl.get(), STOP);
    }

    public boolean isTerminating() {
        int c = ctl.get();
        return runStateAtLeast(c, SHUTDOWN) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        mainLock.lock();
        try {
            while (runStateLessThan(ctl.get(), TERMINATED)) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
        return true;
    }

    public int getCompletedTasks() { return completedTasks; }

    public int getLargestPoolSize() { return largestPoolSize; }
}

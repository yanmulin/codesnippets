package io.yanmulin.codesnippets.examples.concurrency;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class SyncMutex {

    static class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) {
                throw new IllegalStateException("lock not acquired");
            }
            if (compareAndSetState(1, 0)) {
                setExclusiveOwnerThread(null);
                return true;
            }
            return false;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }
    }

    private final Sync sync = new Sync();

    public void lock() { sync.acquire(1); }
    public void unlock() { sync.release(1); }

}

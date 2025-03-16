package io.yanmulin.codesnippets.examples.concurrency;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class SyncOneShotLatch {

    static class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected int tryAcquireShared(int unused) {
            return getState() == 1 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int unused) {
            setState(1);
            return true;
        }
    }

    private final Sync sync = new Sync();

    public void signal() { sync.releaseShared(1); }
    public void await() throws InterruptedException { sync.acquireSharedInterruptibly(1); }
}

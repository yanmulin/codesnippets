package io.yanmulin.codesnippets.examples.concurrency;

import java.util.concurrent.atomic.AtomicReference;

// https://github.com/javaf/clh-lock/tree/master
public class CLHLock {

    class QNode {
        volatile boolean locked = false;
    }

    AtomicReference<QNode> tail = new AtomicReference<>(new QNode());
    ThreadLocal<QNode> node = ThreadLocal.withInitial(() -> new QNode());
    ThreadLocal<QNode> prev = ThreadLocal.withInitial(() -> null);

    public void lock() {
        QNode n = node.get();
        n.locked = true;
        QNode m = tail.getAndSet(n);
        prev.set(m);
        while (m.locked) Thread.yield();
    }

    public void unlock() {
        QNode n = node.get();
        n.locked = false;
        node.set(prev.get()); // recycle prev node because it is unreachable
    }

}

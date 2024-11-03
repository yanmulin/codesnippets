package io.yanmulin.codesnippets.examples.concurrency;

import java.util.concurrent.atomic.AtomicReference;

public class MCSLock {

    static class QNode {
        volatile boolean locked = false;
        volatile QNode next = null;
    }

    AtomicReference<QNode> tail = new AtomicReference<>(null);
    ThreadLocal<QNode> node = new ThreadLocal<>();

    public void lock() {
        QNode n = new QNode();
        n.locked = true;
        node.set(n);
        QNode prev = tail.getAndSet(n);
        if (prev != null) {
            prev.next = n;
            while (n.locked) Thread.yield();
        }
    }

    public void unlock() {
        QNode n = node.get();
        if (n.next == null) {
            if (tail.compareAndSet(n, null))
                return;
            while (n.next == null) Thread.yield();
        }
        n.next.locked = false;
    }
}

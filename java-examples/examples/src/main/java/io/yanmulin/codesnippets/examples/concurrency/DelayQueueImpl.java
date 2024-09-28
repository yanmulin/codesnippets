package io.yanmulin.codesnippets.examples.concurrency;

import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueueImpl<E extends Delayed> {
    PriorityQueue<E> queue = new PriorityQueue<>();
    ReentrantLock lock = new ReentrantLock();
    Thread leader;
    Condition availability = lock.newCondition();

    public void offer(E element) throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            queue.offer(element);
            if (queue.peek() == element) {
                leader = null;
                availability.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (true) {
                E first = queue.peek();
                if (first == null) { // empty queue
                    availability.await();
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) { // element expires
                        return queue.poll();
                    }
                    first = null; // give up reference for faster GC
                    if (leader != null) {
                        availability.await(); // wait leader
                    } else {
                        Thread currentThread = Thread.currentThread();
                        leader = currentThread;
                        try {
                            availability.awaitNanos(delay); // wait for element expiration
                        } finally {
                            if (leader == currentThread) {
                                leader = null; // reset leader
                            }
                        }
                    }
                }
            }
        } finally {
            if (leader == null && queue.peek() != null) { // no leader waiting and more elements to process
                availability.signal();
            }
            lock.unlock();
        }
    }

}

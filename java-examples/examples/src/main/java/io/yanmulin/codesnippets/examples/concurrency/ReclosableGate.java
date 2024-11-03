package io.yanmulin.codesnippets.examples.concurrency;


import java.util.concurrent.atomic.AtomicInteger;

public class ReclosableGate {

    boolean isOpen = false;
    int generation = 0;
    AtomicInteger waitingThreads = new AtomicInteger();

    public synchronized void await() throws InterruptedException {
        waitingThreads.incrementAndGet();
        try {
            int arrivalGeneration = generation;
            while (!isOpen && generation == arrivalGeneration) {
                wait();
            }
        } finally {
            waitingThreads.decrementAndGet();
        }
    }

    public synchronized void open() {
        isOpen = true;
        notifyAll();
    }

    public synchronized void close() {
        isOpen = false;
        generation ++;
        notifyAll();
    }
}

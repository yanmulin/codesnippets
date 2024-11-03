package io.yanmulin.codesnippets.examples.gc;

import java.util.concurrent.CountDownLatch;

public class ThreadOutOfMemory {

    static class HoldThread extends Thread {
        CountDownLatch cdl = new CountDownLatch(1);

        @Override
        public void run() {
            try {
                cdl.await();
            } catch (InterruptedException e) {}
        }
    }

    public static void main(String[] args) {
        for (int i=0;;i ++) {
            System.out.println("i = " + i);
            new Thread(new HoldThread()).start();
        }
    }
}

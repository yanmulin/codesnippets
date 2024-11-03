package io.yanmulin.codesnippets.examples.concurrency;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {
    public class LoggerThread extends Thread {
        @Override
        public void run() {
            String message;
            try {
                while (!isShutdown()) {
                    try {
                        message = queue.take();
                        synchronized (LogWriter.this) {
                            reservations--;
                        }
                        writer.println(message);
                    } catch (InterruptedException exception) {}
                }
            } finally {
                writer.close();
            }
        }

        private boolean isShutdown() {
            synchronized (LogWriter.this) {
                return isShutdown && reservations == 0;
            }
        }
    }

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final PrintWriter writer = new PrintWriter("my.log");
    private final LoggerThread thread = new LoggerThread();
    private boolean isShutdown = false;
    private int reservations = 0;

    public LogWriter() throws FileNotFoundException {}

    public void start() { thread.start(); }

    public synchronized void stop() throws InterruptedException {
        isShutdown = true;
        thread.interrupt();
    }

    public void log(String message) throws InterruptedException {
        synchronized (this) {
            if (isShutdown) throw new IllegalStateException("already shut down");
            reservations ++;
        }
        queue.put(message);
    }

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        LogWriter logWriter = new LogWriter();
        logWriter.start();
        logWriter.log("hello world");
        logWriter.log("yes");
        logWriter.log("interesting");
        logWriter.stop();
        logWriter.thread.join();
    }
}

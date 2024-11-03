package io.yanmulin.codesnippets.examples.concurrency;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogWriterService {

    private static final long TIMEOUT = 100;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final PrintWriter writer = new PrintWriter("my.log");

    public LogWriterService() throws FileNotFoundException {}

    public void log(String message) {
        executor.submit(() -> writer.println(message));
    }

    public void stop() throws InterruptedException {
        try {
            executor.shutdown();
            executor.awaitTermination(TIMEOUT, UNIT);
        } finally {
            writer.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        LogWriter logWriter = new LogWriter();
        logWriter.start();
        logWriter.log("hello world");
        logWriter.log("yes");
        logWriter.log("interesting");
        logWriter.stop();
    }
}

package io.yanmulin.codesnippets.examples.concurrency;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TimerExamples {

    static class SleepTask extends TimerTask {
        @Override
        public void run() {
            long millis = System.currentTimeMillis();
            System.out.println("current timestamp: " + millis);
            try {
                SECONDS.sleep(3);
            } catch (InterruptedException ignore) {}
        }
    }

    static class ThrowTask extends TimerTask {
        @Override
        public void run() {
            throw new RuntimeException();
        }
    }

    void accuracy() throws InterruptedException {
        Timer timer = new Timer();
        timer.schedule(new SleepTask(), 1, 1);
        SECONDS.sleep(3);
    }

    void exception() throws InterruptedException {
        Timer timer = new Timer();
        timer.schedule(new ThrowTask(), 1);
        SECONDS.sleep(1);
        // expect: java.lang.IllegalStateException: Timer already cancelled.
        timer.schedule(new ThrowTask(), 1);
        SECONDS.sleep(5);
    }

    public static void main(String[] args) throws InterruptedException {
        new TimerExamples().accuracy();
        new TimerExamples().exception();
    }
}

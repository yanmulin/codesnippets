package io.yanmulin.codesnippets.examples.basic;

import java.util.Timer;
import java.util.TimerTask;

public class TimerExamples {
    public void schedule() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Task executed after delay.");
            }
        };
        // Schedule the task to execute after 3 seconds (3000 milliseconds)
        timer.schedule(task, 3000);
        System.out.println("Task scheduled");
    }

    public static void main(String[] args) {
        new TimerExamples().schedule();
    }
}

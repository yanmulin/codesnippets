package io.yanmulin.codesnippets.patterns.singleton;

import java.util.concurrent.*;

public class ThreadSafeLazySingleton {
    private static ThreadSafeLazySingleton INSTANCE = null;

    private ThreadSafeLazySingleton() {}

    public static ThreadSafeLazySingleton getInstance() {
        if (INSTANCE == null) {
            synchronized (ThreadSafeLazySingleton.class) {
                if (INSTANCE == null) {
                    System.out.println("creating singleton");
                    INSTANCE = new ThreadSafeLazySingleton();
                }
            }
        }
        return INSTANCE;
    }

    public static void main(String[] args) {
        CountDownLatch go = new CountDownLatch(1);
        Future<ThreadSafeLazySingleton>[] futures = new Future[100];

        for (int i=0;i<futures.length;i++) {
            CompletableFuture<ThreadSafeLazySingleton> f = new CompletableFuture<>();
            futures[i] = f;
            new Thread(() -> {
                try {
                    go.await();
                    f.complete(ThreadSafeLazySingleton.getInstance());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        go.countDown();
        try {
            ThreadSafeLazySingleton singleton = futures[0].get();
            for (Future<ThreadSafeLazySingleton> f: futures) {
                assert singleton == f.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

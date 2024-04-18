package io.yanmulin.codesnippets.patterns.singleton;

public class ThreadLocalSingleton {
    private ThreadLocalSingleton() {}

    private static final ThreadLocal<ThreadLocalSingleton> INSTANCE = ThreadLocal.withInitial(
            () -> new ThreadLocalSingleton());

    public static ThreadLocalSingleton getInstance() { return INSTANCE.get(); }

    public static void main(String[] args) {
        try {
            new Thread(() -> System.out.println(ThreadLocalSingleton.getInstance())).start();
            new Thread(() -> System.out.println(ThreadLocalSingleton.getInstance())).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

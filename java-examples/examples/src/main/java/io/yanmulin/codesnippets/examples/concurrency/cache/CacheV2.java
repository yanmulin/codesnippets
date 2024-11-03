package io.yanmulin.codesnippets.examples.concurrency.cache;

import java.util.concurrent.*;

public class CacheV2<A, V> implements Computable<A, V> {
    private final ConcurrentMap<A, FutureTask<V>> cache = new ConcurrentHashMap<>();
    private final Computable<A, V> c;

    public CacheV2(Computable<A, V> c) {
        this.c = c;
    }

    @Override
    public V compute(A arg) throws InterruptedException {
        Future<V> f = cache.get(arg);
        if (f == null) {
            FutureTask<V> ft = new FutureTask<>(() -> c.compute(arg));
            cache.put(arg, ft);
            ft.run();
            f = ft;
        }
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw launderThrowable(e.getCause());
        }
    }

    private static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) return (RuntimeException)t;
        else if (t instanceof Error) throw (Error)t;
        else throw new IllegalStateException("uncheck exception", t);
    }
}

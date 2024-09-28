package io.yanmulin.codesnippets.benchmark.concurrency;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.time.Instant;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class DelayQueueBenchmark {
    class D implements Delayed {
        long expireMillis;

        public D(long delayMillis) {
            expireMillis = Instant.now().toEpochMilli() + delayMillis;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireMillis - Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int)(getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    D[] ds;
    DelayQueue<D> q;

    @Setup
    public void init() {
        q = new DelayQueue<>();
        ds = new D[10];
        for (int i=0; i<ds.length; i++) {
            ds[i] = new D(i * 100);
        }
    }

    @Benchmark
    public void baseline() throws InterruptedException {
        q.offer(ds[0]);
        q.take();
    }
}

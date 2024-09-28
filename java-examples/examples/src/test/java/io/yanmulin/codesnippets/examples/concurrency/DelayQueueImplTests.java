package io.yanmulin.codesnippets.examples.concurrency;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueImplTests {

    class D implements Delayed {

        Instant expiration;

        public D(long amount, TimeUnit unit) {
            Duration delay = Duration.of(amount, unit.toChronoUnit());
            expiration = Instant.now().plus(delay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(Duration.between(Instant.now(), expiration));
        }

        @Override
        public int compareTo(Delayed o) {
            return (int)(getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
        }
    }

    private DelayQueueImpl<D> q;

    @Test
    public void testSingle() throws InterruptedException {
        q = new DelayQueueImpl<>();
        long millis = System.currentTimeMillis();
        q.offer(new D(100, TimeUnit.MILLISECONDS));
        q.take();
        Assert.assertTrue(System.currentTimeMillis() - millis >= 100);
    }

    @Test
    public void testMulti() throws InterruptedException {
        q = new DelayQueueImpl<>();
        long millis = System.currentTimeMillis();
        q.offer(new D(300, TimeUnit.MILLISECONDS));
        q.offer(new D(500, TimeUnit.MILLISECONDS));
        q.offer(new D(100, TimeUnit.MILLISECONDS));
        q.take();
        Assert.assertTrue(System.currentTimeMillis() - millis >= 100);
        Assert.assertTrue(System.currentTimeMillis() - millis < 300);
        q.take();
        Assert.assertTrue(System.currentTimeMillis() - millis >= 300);
        Assert.assertTrue(System.currentTimeMillis() - millis < 500);
        q.take();
        Assert.assertTrue(System.currentTimeMillis() - millis >= 500);
    }
}

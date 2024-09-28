package io.yanmulin.codesnippets.benchmark.concurrency;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@State(Scope.Benchmark)
public class BlockingQueueBenchmark {
    static Object obj = new Object();
    BlockingQueue<Object> queue;
    @Param({"linked", "array"})
    String type;

    @Setup
    public void init() {
        switch (type) {
            case "linked": queue = new LinkedBlockingQueue<>(); break;
            case "array": queue = new ArrayBlockingQueue<>(1024); break;
            default: throw new IllegalArgumentException("unknown queue type: " + type);
        }
    }

    @Benchmark
    public void baseline() {
        queue.offer(obj);
        queue.poll();
    }

}

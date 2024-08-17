package io.yanmulin.onebrc.v1;

import io.yanmulin.onebrc.support.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class Streaming {

    private final static int N_THREADS = 32;
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";
    private final static ObjectPool.PooledObject<Chunk> POISON = new ObjectPool.PooledObject<>(new Chunk(0), null);
    private final static ChunkFactory CHUNK_FACTORY = new ChunkFactory(BUFFER_SIZE);
    private final static ObjectPool<Chunk> POOL = new ObjectPool<>(CHUNK_FACTORY);
    private final static Collector<ThreadResultRow, AggregateMeasurement, ResultRow> collector = Collector.of(
            AggregateMeasurement::new,
            (agg, row) -> {
                agg.max = Math.max(agg.max, row.max);
                agg.min = Math.min(agg.min, row.min);
                agg.sum += row.sum;
                agg.count += row.count;
            },
            (agg1, agg2) -> {
                AggregateMeasurement agg = new AggregateMeasurement();
                agg.max = Math.max(agg1.max, agg2.max);
                agg.min = Math.min(agg1.min, agg2.min);
                agg.sum = agg1.sum + agg2.sum;
                agg.count = agg1.count + agg2.count;
                return agg;
            },
            agg -> new ResultRow(agg.min, agg.sum / agg.count, agg.max)
    );

    private static class LineContext {
        int current = 0;
        int size = 0;
        float temperature = 0.f;
        int lineStart = 0;
        int cityNameLength = 0;
        Integer cityHash = 0;
    }

    private static void parseCity(byte[] buffer, LineContext context) {
        int hash = 1, pos;
        pos = context.lineStart = context.current;
        while (pos < context.size && buffer[pos] != ';') {
            hash = 31 * hash + buffer[pos];
            pos ++;
        }

        context.cityNameLength = pos - context.lineStart;
        context.current = pos + 1;
        context.cityHash = hash;
    }

    private static void parseTemperature(byte[] buffer, LineContext context) {
        int x = 0, divider = 0, sign = 1, pos;

        for (pos=context.current; pos < context.size && buffer[pos] != '\n'; pos ++) {
            if (buffer[pos] == 45) {
                sign = -1;
            } else if (buffer[pos] == 46) {
                divider = 1;
            } else {
                x = (x * 10) + (buffer[pos] - 48);
                if (divider > 0) divider *= 10;
            }
        }
        context.current = pos + 1;
        context.temperature = (float)sign * x / divider;
    }

    private static void doLine(byte[] buffer, LineContext context, Map<Integer, ThreadResultRow> aggregate) {
        parseCity(buffer, context);
        parseTemperature(buffer, context);
        ThreadResultRow row = aggregate.computeIfAbsent(context.cityHash, (_) -> {
            String city = new String(buffer, context.lineStart, context.cityNameLength);
            return new ThreadResultRow(city);
        });

        float temperature = context.temperature;
        row.min = Math.min(row.min, temperature);
        row.max = Math.max(row.max, temperature);
        row.sum += temperature;
        row.count += 1;
    }

    private static void doChunk(Map<Integer, ThreadResultRow> aggregate, Chunk chunk) {
        LineContext context = new LineContext();
        context.size = chunk.size;
        while (context.current < chunk.size) {
            doLine(chunk.buffer, context, aggregate);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        BlockingQueue<ObjectPool.PooledObject<Chunk>> input = new LinkedBlockingQueue<>();
        BlockingQueue<ThreadResultRow> output = new LinkedBlockingQueue<>();
        Thread[] threads = new Thread[N_THREADS];
        for (int i=0;i<N_THREADS;i++) {
            threads[i] = new Thread(() -> {
                ObjectPool.PooledObject<Chunk> chunk;
                Map<Integer, ThreadResultRow> aggregate = new HashMap<>();
                do {
                    try {
                        chunk = input.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (chunk.equals(POISON)) break;
                    doChunk(aggregate, chunk.get());
                    chunk.release();
                } while (true);

                for (ThreadResultRow row: aggregate.values()) {
                    output.offer(row);
                }
            });
            threads[i].start();
        }

        int n;
        int chunks = 0, copy = 0, bytes = 0;
        try (FileInputStream reader = new FileInputStream(PATH)) {
            ObjectPool.PooledObject<Chunk> pooledChunk = POOL.create();
            Chunk chunk = pooledChunk.get();
            while ((n = reader.read(chunk.buffer, chunk.size, BUFFER_SIZE - chunk.size)) >= 0) {
                bytes += n;
                chunk.size += n;

                ObjectPool.PooledObject<Chunk> nextPooledChunk = POOL.create();
                Chunk nextChunk = nextPooledChunk.get();
                int pos = chunk.size - 1;
                while (pos > 0 && chunk.buffer[pos - 1] != '\n') {
                    pos --;
                }

                if (pos <= 0) {
                    throw new IllegalStateException("buffer size too small");
                }

                while (pos < chunk.size) {
                    nextChunk.buffer[nextChunk.size] = chunk.buffer[pos];
                    nextChunk.size ++;
                    pos ++;
                }

                chunk.size -= nextChunk.size;
                copy += nextChunk.size;

                input.offer(pooledChunk);
                pooledChunk = nextPooledChunk;
                chunk = pooledChunk.get();
                chunks ++;
            }

            if (chunk.size > 0) {
                input.offer(pooledChunk);
            }
        }

        for (int i=0;i<N_THREADS;i++) {
            input.offer(POISON);
        }
        for (int i=0;i<N_THREADS;i++) {
            threads[i].join();
        }

        List<ThreadResultRow> rows = new ArrayList<>();
        while (!output.isEmpty()) {
            output.drainTo(rows);
        }

        Map<String, ResultRow> result = new TreeMap<>(rows.stream()
                .collect(Collectors.groupingBy(row -> row.city, collector)));
        System.out.println(result);

        long elapsed = System.currentTimeMillis() - start;
        int records = rows.stream().mapToInt(row -> row.count).sum();
        System.out.println("read " + bytes + " bytes/" + chunks + " chunks");
        System.out.println("created " + CHUNK_FACTORY.countCreated() + " chunks");
        System.out.println("copied " + copy + " bytes");
        System.out.println("elapsed " + elapsed + " ms");
        System.out.println("total " + records + " records");
    }
}

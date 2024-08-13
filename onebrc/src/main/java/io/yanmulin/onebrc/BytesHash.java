package io.yanmulin.onebrc;

import dev.morling.onebrc.support.*;

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
public class BytesHash {

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

    private static float parseFloat(byte[] buffer, int start, int end) {
        int x = 0, divider = 0, sign = 1;
        for (int i=start;i<end;i++) {
            if (buffer[i] == 45) {
                sign = -1;
            } else if (buffer[i] == 46) {
                divider = 1;
            } else {
                x = (x * 10) + (buffer[i] - 48);
                if (divider > 0) divider *= 10;
            }
        }
        return (float) sign * x / divider;
    }

    private static int hashCode(byte[] a, int fromIndex, int toIndex) {
        int result = 1;
        for (int i = fromIndex; i < toIndex; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }

    private static void doLine(Chunk chunk, int pos, int lineStart, int semicolonPos,
                               Map<Integer, ThreadResultRow> aggregate) {
        int hash = hashCode(chunk.buffer, lineStart, semicolonPos);
        float temperature = parseFloat(chunk.buffer, semicolonPos + 1, pos);
        aggregate.compute(hash, (_, row) -> {
            if (row == null) {
                String city = new String(chunk.buffer, lineStart, semicolonPos - lineStart);
                row = new ThreadResultRow(city);
            }
            row.min = Math.min(row.min, temperature);
            row.max = Math.max(row.max, temperature);
            row.sum += temperature;
            row.count += 1;
            return row;
        });
    }

    private static void doChunk(Map<Integer, ThreadResultRow> aggregate, Chunk chunk) {
        int lineStart = 0, semicolonPos = 0;

        for (int i=0;i<chunk.size;i++) {
            if (chunk.buffer[i] == ';') {
                semicolonPos = i;
            } else if (chunk.buffer[i] == '\n') {
                doLine(chunk, i, lineStart, semicolonPos, aggregate);
                lineStart = i + 1;
            }
        }

        if (lineStart != chunk.size) {
            doLine(chunk, chunk.size, lineStart, semicolonPos, aggregate);
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

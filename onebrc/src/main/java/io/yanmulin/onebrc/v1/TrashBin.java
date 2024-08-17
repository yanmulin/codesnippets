package io.yanmulin.onebrc.v1;


import io.yanmulin.onebrc.support.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class TrashBin {
    private final static int N_THREADS = 16;
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";
    private final static Object lock = new Object();
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
    private static AtomicInteger nextChunkId = new AtomicInteger();

    private static class LineContext {
        int current = 0;
        int size = 0;
        float temperature = 0.f;
        int lineStart = 0;
        int cityNameLength = 0;
        Integer cityHash;
    }

    public static int readChunk(InputStream inputStream, Chunk chunk) {
        int n, chunkId = 0;
        synchronized (lock) {
            try {
                n = inputStream.read(chunk.buffer, 0, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                n = -1;
            }
//            chunkId = nextChunkId.getAndIncrement();
        }
        if (n >= 0) {
            chunk.size = n;
        }
        return n >= 0 ? chunkId : -1;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[N_THREADS];
        BlockingQueue<ThreadResultRow> output = new LinkedBlockingQueue<>();

        try (FileInputStream reader = new FileInputStream(PATH)) {
            for (int i = 0; i < N_THREADS; i++) {
                threads[i] = new Thread(() -> {
                    int chunkId;
                    Chunk chunk = new Chunk(BUFFER_SIZE);
                    Map<Integer, ThreadResultRow> aggregate = new HashMap<>();
                    while ((chunkId = readChunk(reader, chunk)) >= 0) {
                        processChunk(chunk, chunkId, aggregate);
                    }
                    for (ThreadResultRow row: aggregate.values()) {
                        output.offer(row);
                    }
                });
                threads[i].start();
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
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed " + elapsed + " ms");
    }

    private static int skipLine(byte[] buffer, int pos) {
        while (buffer[pos] != '\n') pos ++;
        return pos + 1;
    }

    private static void processChunk(Chunk chunk, int chunkId, Map<Integer, ThreadResultRow> aggregate) {
        LineContext context = new LineContext();
        context.current = skipLine(chunk.buffer, 0);
        context.size = chunk.size;
        while (context.current < chunk.size) {
            processLine(chunk.buffer, context, aggregate);
        }
        context.current = chunk.size;
    }

    private static boolean parseCity(byte[] buffer, LineContext context) {
        int hash = 1, pos = context.current;
        context.lineStart = pos;
        while (pos < context.size && buffer[pos] != ';') {
            hash = 31 * hash + buffer[pos];
            pos ++;
        }

        if (pos < context.size) {
            context.cityNameLength = pos - context.lineStart;
            context.current = pos + 1;
            context.cityHash = hash;
            return true;
        }
        context.current = context.size;
        return false;
    }

    private static boolean parseTemperature(byte[] buffer, LineContext context) {
        int x = 0, divider = 0, sign = 1;
        int pos = context.current;

        while (pos < context.size && buffer[pos] != '\n') {
            if (buffer[pos] == '-') {
                sign = -1;
            } else if (buffer[pos] == '.') {
                divider = 1;
            } else {
                x = (x * 10) + (buffer[pos] - 48);
                if (divider > 0) divider *= 10;
            }
            pos ++;
        }
        if (pos < context.size) {
            context.current = pos + 1;
            context.temperature = (float) sign * x / divider;
            return true;
        }
        context.current = context.size;
        return false;
    }

    private static void processLine(byte[] buffer, LineContext context, Map<Integer, ThreadResultRow> aggregate) {
        if (!parseCity(buffer, context)) return;
        if (!parseTemperature(buffer, context)) return;
        aggregate.compute(context.cityHash, (_, row) -> {
            if (row == null) {
                String city = new String(buffer, context.lineStart, context.cityNameLength);
                row = new ThreadResultRow(city);
            }
            float temperature = context.temperature;
            row.min = Math.min(row.min, temperature);
            row.max = Math.max(row.max, temperature);
            row.sum += temperature;
            row.count += 1;
            return row;
        });
    }
}

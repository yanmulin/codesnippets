package io.yanmulin.onebrc;

import dev.morling.onebrc.support.AggregateMeasurement;
import dev.morling.onebrc.support.Chunk;
import dev.morling.onebrc.support.ResultRow;
import dev.morling.onebrc.support.ThreadResultRow;

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
public class ThreadedChunk {

    private final static int N_THREADS = 32;
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";
    private final static Chunk POISON = new Chunk(0);
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

    private static void doLine(Chunk chunk, int pos, int lineStart, int semicolonPos,
                               Map<String, ThreadResultRow> aggregate) {
        String city = new String(chunk.buffer, lineStart, semicolonPos - lineStart);
        float temperature = Float.parseFloat(new String(chunk.buffer, semicolonPos + 1, pos - semicolonPos - 1));
        aggregate.compute(city, (_, row) -> {
            row = row == null ? new ThreadResultRow(city) : row;
            row.min = Math.min(row.min, temperature);
            row.max = Math.max(row.max, temperature);
            row.sum += temperature;
            row.count += 1;
            return row;
        });
    }

    private static void doChunk(Map<String, ThreadResultRow> aggregate, Chunk chunk) {
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

        BlockingQueue<Chunk> input = new LinkedBlockingQueue<>();
        BlockingQueue<ThreadResultRow> output = new LinkedBlockingQueue<>();
        Thread[] threads = new Thread[N_THREADS];
        for (int i=0;i<N_THREADS;i++) {
            threads[i] = new Thread(() -> {
                Chunk chunk;
                Map<String, ThreadResultRow> aggregate = new HashMap<>();
                do {
                    try {
                        chunk = input.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (chunk.equals(POISON)) break;
                    doChunk(aggregate, chunk);
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
            Chunk chunk = new Chunk(BUFFER_SIZE);
            while ((n = reader.read(chunk.buffer, chunk.size, BUFFER_SIZE - chunk.size)) >= 0) {
                bytes += n;
                chunk.size += n;

                Chunk nextChunk = new Chunk(BUFFER_SIZE);
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

                input.offer(chunk);
                chunk = nextChunk;
                chunks ++;
            }

            if (chunk.size > 0) {
                input.offer(chunk);
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
                .collect(Collectors.groupingBy(r -> r.city, collector)));
        System.out.println(result);

        long elapsed = System.currentTimeMillis() - start;
        int records = rows.stream().mapToInt(r -> r.count).sum();
        System.out.println("read " + bytes + " bytes");
        System.out.println("created " + chunks + " chunks");
        System.out.println("copied " + copy + " bytes");
        System.out.println("elapsed " + elapsed + " ms");
        System.out.println("total " + records + " records");
    }
}

package io.yanmulin.onebrc.v2;

import io.yanmulin.onebrc.support.AggregateMeasurement;
import io.yanmulin.onebrc.support.HashThreadResultRow;
import io.yanmulin.onebrc.support.IntegerThreadResultRow;
import io.yanmulin.onebrc.support.ResultRow;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class WorkStealing {

    private final static int CHUNK_SIZE = 8192;
    private static final String PATH = "./measurements.txt";
    private static final int HASH_TABLE_SIZE = 2048;
    private static final Unsafe UNSAFE = unsafe();
    private static final Collector<IntegerThreadResultRow, AggregateMeasurement, ResultRow> collector = Collector.of(
            AggregateMeasurement::new,
            (agg, row) -> {
                agg.max = Math.max(agg.max, row.max / 10.0f);
                agg.min = Math.min(agg.min, row.min / 10.0f);
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

    static AtomicLong chunkSelector = new AtomicLong();

    static class ChunkProcessor extends Thread {
        long address;
        long chunkCount;
        long fileLength;
        long current;
        HashThreadResultRow[] table;
        IntegerThreadResultRow[][] results;
        int index;

        public ChunkProcessor(long address, long chunkCount, long fileLength, IntegerThreadResultRow[][] results, int index) {
            this.address = address;
            this.chunkCount = chunkCount;
            this.fileLength = fileLength;
            this.results = results;
            this.index = index;
            this.current = 0;
            this.table = new HashThreadResultRow[HASH_TABLE_SIZE];
        }

        @Override
        public void run() {
            while (true) {
                long selectedChunk = chunkSelector.getAndIncrement();
                if (selectedChunk >= chunkCount) break;

                long baseAddress = address + CHUNK_SIZE * selectedChunk;
                long limitAddress = address + Math.min(fileLength, CHUNK_SIZE * (selectedChunk + 1));

                if (selectedChunk > 0) {
                    baseAddress --;
                    while (UNSAFE.getByte(baseAddress) != '\n') {
                        baseAddress ++;
                    }
                    baseAddress ++;
                }

                current = baseAddress;
                while (current < limitAddress) {
                    processLine();
                }
            }
            results[index] = Arrays.stream(table).filter(Objects::nonNull)
                    .map(row -> IntegerThreadResultRow.of(row, UNSAFE))
                    .toArray(IntegerThreadResultRow[]::new);

        }

        private int parseCityHash() {
            byte b;
            int hash = 1;
            while ((b = UNSAFE.getByte(current)) != ';') {
                hash = 31 * hash + b;
                current ++;
            }
            return hash;
        }

        private int parseTemperature() {
            byte b;
            int x = 0, sign = 1;

            while ((b = UNSAFE.getByte(current)) != '\n') {
                if (b == 45) {
                    sign = -1;
                } else if ('0' <= b && b <= '9') {
                    x = (x * 10) + (b - 48);
                }
                current ++;
            }
            return sign * x;
        }

        private void processLine() {
            long lineStartPos = current;
            int hash = parseCityHash();
            long semicolonPos = current;
            current ++;
            int temperature = parseTemperature();
            current ++;

            HashThreadResultRow row = findInTable(hash, lineStartPos, semicolonPos - lineStartPos);
            row.min = Math.min(row.min, temperature);
            row.max = Math.max(row.max, temperature);
            row.sum += temperature;
            row.count += 1;
        }

        private HashThreadResultRow findInTable(int hash, long cityOffset, long cityLength) {
            int index = hash & (HASH_TABLE_SIZE - 1);
            while (table[index] != null && !cityEquals(table[index], hash, cityOffset, cityLength)) {
                index = (index + 1) & (HASH_TABLE_SIZE - 1);
            }
            if (table[index] == null) {
                table[index] = new HashThreadResultRow(hash, cityOffset, cityLength);
            }
            return table[index];
        }

        private boolean cityEquals(HashThreadResultRow row, int hash, long cityOffset, long cityLength) {
            if (row.hash != hash) return false;
            if (row.cityLength != cityLength) return false;
            for (long i=0;i<cityLength;i++) {
                if (UNSAFE.getByte(row.cityOffset + i) != UNSAFE.getByte(cityOffset + i)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        long start = System.currentTimeMillis();
        final int threadsNum = Runtime.getRuntime().availableProcessors();

        IntegerThreadResultRow[][] results = new IntegerThreadResultRow[threadsNum][];
        Thread[] threads = new Thread[threadsNum];
        try (RandomAccessFile file = new RandomAccessFile(PATH, "r")) {

            MemorySegment segment = file.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, file.length(), Arena.global());
            long chunkCount = Math.ceilDiv(file.length(), CHUNK_SIZE);

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new ChunkProcessor(segment.address(), chunkCount, file.length(), results, i);
                threads[i].start();
            }
        }

        for (int i = 0; i < threadsNum; i++) {
            threads[i].join();
        }

        Map<String, ResultRow> result = new TreeMap<>(Arrays.stream(results).flatMap(Arrays::stream)
                .collect(Collectors.groupingBy(row -> row.city, collector)));
        System.out.println(result);

        long elapsed = System.currentTimeMillis() - start;
        int records = Arrays.stream(results).flatMap(Arrays::stream).mapToInt(row -> row.count).sum();
        System.out.println("elapsed " + elapsed + " ms");
        System.out.println("total " + records + " records");
    }

    private static Unsafe unsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(Unsafe.class);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

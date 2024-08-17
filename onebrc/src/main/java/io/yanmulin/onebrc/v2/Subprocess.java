package io.yanmulin.onebrc.v2;

import io.yanmulin.onebrc.support.AggregateMeasurement;
import io.yanmulin.onebrc.support.HashThreadResultRow;
import io.yanmulin.onebrc.support.IntegerThreadResultRow;
import io.yanmulin.onebrc.support.ResultRow;
import org.checkerframework.checker.units.qual.A;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class Subprocess {
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

    static class ChunkProcessor extends Thread {

        long address;
        long size;
        long current;
        HashThreadResultRow[] table;
        IntegerThreadResultRow[][] results;
        int index;

        public ChunkProcessor(long address, long size, IntegerThreadResultRow[][] results, int index) {
            this.address = address;
            this.size = size;
            this.results = results;
            this.index = index;
            this.current = address;
            this.table = new HashThreadResultRow[HASH_TABLE_SIZE];
        }

        @Override
        public void run() {
            while (current < address + size) {
                processLine();
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

        if (args.length == 0 || !"--worker".equals(args[0])) {
            spawnWorker();
            return;
        }

        long start = System.currentTimeMillis();
        final int threadsNum = Runtime.getRuntime().availableProcessors();

        long[] chunkOffsets = new long[threadsNum + 1];
        try (RandomAccessFile file = new RandomAccessFile(PATH, "r")) {
            long chunkSize = Math.floorDiv(file.length(), threadsNum + 1);
            long offset = 0;
            for (int i = 0; i < threadsNum; i++) {
                chunkOffsets[i] = offset;
                file.seek(offset + chunkSize);
                while (file.read() != '\n') {
                }
                offset = file.getFilePointer();
            }
            chunkOffsets[threadsNum] = file.length();
        }

        IntegerThreadResultRow[][] results = new IntegerThreadResultRow[threadsNum][];
        Thread[] threads = new Thread[threadsNum];
        try (RandomAccessFile file = new RandomAccessFile(PATH, "r")) {

            MemorySegment segment = file.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, file.length(), Arena.global());
            long address = segment.address();

            for (int i = 0; i < threads.length; i++) {
                long size = chunkOffsets[i + 1] - chunkOffsets[i];
                threads[i] = new ChunkProcessor(address + chunkOffsets[i], size, results, i);
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
        System.out.close();
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

    private static void spawnWorker() throws IOException {
        ProcessHandle.Info info = ProcessHandle.current().info();
        List<String> commands = new ArrayList<>();
        info.command().ifPresent(commands::add);
        info.arguments().ifPresent(args -> commands.addAll(Arrays.asList(args)));
        commands.add("--worker");
        new ProcessBuilder().command(commands).inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start().getInputStream().transferTo(System.out);
    }
}
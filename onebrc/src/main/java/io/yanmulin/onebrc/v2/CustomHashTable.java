package io.yanmulin.onebrc.v2;

import io.yanmulin.onebrc.support.AggregateMeasurement;
import io.yanmulin.onebrc.support.HashThreadResultRow;
import io.yanmulin.onebrc.support.IntegerThreadResultRow;
import io.yanmulin.onebrc.support.ResultRow;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class CustomHashTable {
    private final static String PATH = "./measurements.txt";
    private final static int HASH_TABLE_SIZE = 2048;
    private final static Collector<IntegerThreadResultRow, AggregateMeasurement, ResultRow> collector = Collector.of(
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

        MemorySegment segment;
        long offset;
        HashThreadResultRow[] table;
        IntegerThreadResultRow[][] results;
        int index;

        public ChunkProcessor(MemorySegment segment, IntegerThreadResultRow[][] results, int index) {
            this.segment = segment;
            this.results = results;
            this.index = index;
            this.offset = 0;
            this.table = new HashThreadResultRow[HASH_TABLE_SIZE];
        }

        @Override
        public void run() {
            long size = segment.byteSize();
            while (offset < size) {
                processLine();
            }
            results[index] = Arrays.stream(table).filter(Objects::nonNull)
                    .map(row -> IntegerThreadResultRow.of(row, segment))
                    .toArray(IntegerThreadResultRow[]::new);
        }

        private int parseCityHash() {
            byte b;
            int hash = 1;
            while ((b = segment.get(ValueLayout.JAVA_BYTE, offset)) != ';') {
                hash = 31 * hash + b;
                offset ++;
            }
            return hash;
        }

        private int parseTemperature() {
            byte b;
            int x = 0, sign = 1;

            while ((b = segment.get(ValueLayout.JAVA_BYTE, offset)) != '\n') {
                if (b == 45) {
                    sign = -1;
                } else if ('0' <= b && b <= '9') {
                    x = (x * 10) + (b - 48);
                }
                offset ++;
            }
            return sign * x;
        }

        private void processLine() {
            long lineStartPos = offset;
            int hash = parseCityHash();
            long semicolonPos = offset;
            offset ++;
            int temperature = parseTemperature();
            offset ++;

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
            MemorySegment slice = segment.asSlice(row.cityOffset, row.cityLength);
            return slice.mismatch(segment.asSlice(cityOffset, cityLength)) == -1;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
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

            MemorySegment map = file.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, 0, file.length(), Arena.global());

            for (int i = 0; i < threads.length; i++) {
                MemorySegment slice = map.asSlice(chunkOffsets[i], chunkOffsets[i + 1] - chunkOffsets[i]);
                threads[i] = new ChunkProcessor(slice, results, i);
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
}

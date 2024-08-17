package io.yanmulin.onebrc.v2;

import io.yanmulin.onebrc.support.*;

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
public class FileMap {
    private final static String PATH = "./measurements.txt";
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

    static class ChunkProcessor extends Thread {

        MemorySegment segment;
        ThreadResultRow[][] results;
        int index;

        public ChunkProcessor(MemorySegment segment, ThreadResultRow[][] results, int index) {
            this.segment = segment;
            this.results = results;
            this.index = index;
        }

        @Override
        public void run() {
            Map<Integer, ThreadResultRow> result = new HashMap<>();
            long size = segment.byteSize(), offset = 0;
            while (offset < size) {
                offset = processLine(segment, offset, result);
            }
            results[index] = result.values().toArray(ThreadResultRow[]::new);
        }

        private static long find(MemorySegment segment, long offset, byte target) {
            long i;
            ValueLayout.OfByte layout = ValueLayout.JAVA_BYTE;
            for (i=offset;segment.get(layout, i)!=target;i++) {}
            return i;
        }

        private static int parseCityHash(MemorySegment segment, long start, long end) {
            int hash = 1;
            for (long i=start;i<end;i++) {
                hash = 31 * hash + segment.get(ValueLayout.JAVA_BYTE, i);
            }
            return hash;
        }

        private static float parseTemperature(MemorySegment segment, long start, long end) {
            int x = 0, divider = 0, sign = 1;

            for (long i=start; i < end; i ++) {
                byte b = segment.get(ValueLayout.JAVA_BYTE, i);
                if (b == 45) {
                    sign = -1;
                } else if (b == 46) {
                    divider = 1;
                } else {
                    x = (x * 10) + (b - 48);
                    if (divider > 0) divider *= 10;
                }
            }
            return (float)sign * x / divider;
        }

        private static long processLine(MemorySegment segment, long offset, Map<Integer, ThreadResultRow> result) {
            long lineStartPos = offset;
            long semicolonPos = find(segment, lineStartPos, (byte) ';');
            long lineEndPos = find(segment, semicolonPos, (byte) '\n');
            int hash = parseCityHash(segment, lineStartPos, semicolonPos);
            float temperature = parseTemperature(segment, semicolonPos + 1, lineEndPos);
            ThreadResultRow row = result.computeIfAbsent(hash, _ -> {
                MemorySegment citySegment = segment.asSlice(lineStartPos, semicolonPos - lineStartPos);
                String city = new String(citySegment.toArray(ValueLayout.JAVA_BYTE));
                return new ThreadResultRow(city);
            });

            row.min = Math.min(row.min, temperature);
            row.max = Math.max(row.max, temperature);
            row.sum += temperature;
            row.count += 1;
            return lineEndPos + 1;
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

        ThreadResultRow[][] results = new ThreadResultRow[threadsNum][];
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

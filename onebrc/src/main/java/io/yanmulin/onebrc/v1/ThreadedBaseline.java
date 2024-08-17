package io.yanmulin.onebrc.v1;

import io.yanmulin.onebrc.support.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Lyuwen Yan
 * @date
 */
public class ThreadedBaseline {

    private final static String PATH = "./measurements.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        Collector<Measurement, AggregateMeasurement, ResultRow> collector = Collector.of(
                AggregateMeasurement::new,
                (agg, m) -> {
                    agg.max = Math.max(agg.max, m.temperature());
                    agg.min = Math.min(agg.min, m.temperature());
                    agg.sum += m.temperature();
                    agg.count += 1;
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

        Map<String, ResultRow> collect = new TreeMap<>(Files.lines(Path.of(PATH))
                .parallel().map(Measurement::of)
                .collect(Collectors.groupingBy(Measurement::city, collector)));
        System.out.println(collect);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed " + elapsed + " milliseconds");
    }
}

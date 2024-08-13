package io.yanmulin.onebrc.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Lyuwen Yan
 * @date
 */
public class FileLinesReader {

    private final static String PATH = "./measurements.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        int count = Files.lines(Path.of(PATH)).mapToInt(_ -> 1).sum();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("total " + count + " lines, elapsed " + elapsed + " ms");
    }
}

package io.yanmulin.onebrc.benchmark;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Lyuwen Yan
 * @date
 */
public class FileCharsReader {
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        int n, chars = 0;
        char[] buffer = new char[BUFFER_SIZE];
        try (FileReader reader = new FileReader(PATH)) {
            while ((n = reader.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                chars += n;
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("read " + chars + " chars, elapsed " + elapsed + " ms");
    }
}

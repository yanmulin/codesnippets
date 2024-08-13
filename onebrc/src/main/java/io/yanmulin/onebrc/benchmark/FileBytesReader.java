package io.yanmulin.onebrc.benchmark;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Lyuwen Yan
 * @date
 */
public class FileBytesReader {
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        int n, bytes = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream reader = new FileInputStream(PATH)) {
            while ((n = reader.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                bytes += n;
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("read " + bytes + " bytes, elapsed " + elapsed + " ms");
    }
}

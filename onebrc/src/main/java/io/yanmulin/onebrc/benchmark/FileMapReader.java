package io.yanmulin.onebrc.benchmark;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Lyuwen Yan
 * @date
 */
public class FileMapReader {

    private final static String PATH = "./measurements.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        try (FileChannel file = FileChannel.open(Path.of(PATH), StandardOpenOption.READ);
             Arena arena = Arena.ofShared()) {
            MemorySegment data = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size(), arena);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("read " + data.byteSize() + " bytes, elapsed " + elapsed + " millis");
        }
    }
}

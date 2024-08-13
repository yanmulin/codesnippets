package io.yanmulin.onebrc.benchmark;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Lyuwen Yan
 * @date
 */
public class FileChunkReader {
    private final static int BUFFER_SIZE = 8192;
    private final static String PATH = "./measurements.txt";

    private static class Chunk {
        byte[] buffer = new byte[BUFFER_SIZE];
        int size = 0;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        int n, chunks = 0, bytes = 0, copy = 0;
        try (InputStream reader = new FileInputStream(PATH)) {
            Chunk chunk = new Chunk();
            while ((n = reader.read(chunk.buffer, chunk.size, BUFFER_SIZE - chunk.size)) >= 0) {
                bytes += n;
                chunk.size += n;

                Chunk nextChunk = new Chunk();
                int pos = chunk.size - 1;
                while (pos > 0 && chunk.buffer[pos] != '\n') {
                    nextChunk.buffer[nextChunk.size] = chunk.buffer[pos];
                    nextChunk.size += 1;
                    pos --;
                }

                if (pos <= 0) {
                    throw new IllegalStateException("buffer size too small");
                }

                chunk.size -= nextChunk.size;
                copy += nextChunk.size;

                chunk = nextChunk;
                chunks ++;
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("read " + bytes + " bytes");
        System.out.println("created " + chunks + " chunks");
        System.out.println("copied " + copy + " bytes");
        System.out.println("elapsed " + elapsed + " ms");
    }
}

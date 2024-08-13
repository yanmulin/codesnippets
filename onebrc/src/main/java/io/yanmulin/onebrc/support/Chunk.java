package io.yanmulin.onebrc.support;

/**
 * @author Lyuwen Yan
 * @date
 */
public class Chunk {
    public byte[] buffer;
    public int size;

    public Chunk(int capacity) {
        buffer = new byte[capacity];
        size = 0;
    }
}

package io.yanmulin.onebrc.support;

/**
 * @author Lyuwen Yan
 * @date
 */
public class ChunkFactory implements ObjectPool.ObjectFactory<Chunk> {

    int capacity;
    int created = 0;

    public ChunkFactory(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Chunk create() {
        created ++;
        return new Chunk(capacity);
    }

    @Override
    public void reset(Chunk obj) {
        obj.size = 0;
    }

    public int countCreated() { return created; }
}
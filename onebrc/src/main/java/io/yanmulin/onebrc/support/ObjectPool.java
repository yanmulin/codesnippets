package io.yanmulin.onebrc.support;

import com.google.common.base.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lyuwen Yan
 * @date
 */
public class ObjectPool<T> {

    public interface ObjectFactory<T> {
        T create();
        void reset(T obj);
    }

    public static class PooledObject<T> {
        T obj;
        ObjectPool<T> pool;
        int refCount;

        public PooledObject(T obj, ObjectPool<T> pool) {
            this.obj = obj;
            this.pool = pool;
            this.refCount = 0;
        }

        public T get() { return obj; }

        public void acquire() {
            refCount ++;
        }

        public void release() {
            Preconditions.checkState(refCount > 0);
            refCount --;
            if (refCount == 0) {
                pool.release(this);
            }
        }
    }

    ObjectFactory<T> factory;

    List<PooledObject<T>> idle = new LinkedList<>();

    public ObjectPool(ObjectFactory<T> factory) {
        this.factory = factory;
    }

    public synchronized PooledObject<T> create() {
        PooledObject<T> pooled;
        if (!idle.isEmpty()) {
            pooled = idle.removeLast();
            pooled.acquire();
            return pooled;
        }
        T obj = factory.create();
        pooled = new PooledObject<>(obj, this);
        pooled.acquire();
        return pooled;
    }

    private synchronized void release(PooledObject<T> pooled) {
        Preconditions.checkState(pooled.refCount == 0);
        factory.reset(pooled.get());
        idle.add(pooled);
    }


}

package io.yanmulin.codesnippets.examples.basic;

import lombok.Getter;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class ReferenceExamples {

    private static class BigObject {
        int id;
        byte[] random;

        public BigObject(int id) {
            this.id = id;
            random = new byte[1024];
        }
    }

    private class SoftEntry extends SoftReference<Object> {
        @Getter
        private int id;

        public SoftEntry(int id, Object referent, ReferenceQueue<Object> q) {
            super(referent, q);
            this.id = id;
        }
    }

    private class WeakEntry extends WeakReference<Object> {
        @Getter
        private int id;

        public WeakEntry(int id, Object referent, ReferenceQueue<Object> q) {
            super(referent, q);
            this.id = id;
        }
    }

    public void soft() {
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        Map<Integer, SoftEntry> cache = new HashMap<>();
        SoftEntry ref;
        long total = 0;

        System.out.println("# SoftReference example #");
        for (int i=0;i<10000;i++) {
            cache.put(i, new SoftEntry(i, new BigObject(i), queue));
            long count = 0;
            while ((ref = (SoftEntry) queue.poll()) != null) {
                if (count == 0) System.out.print(i + ": garbage collected ");
                System.out.print(ref.getId() + " ");
                count ++;
            }
            if (count > 0) System.out.println();
            total += count;
        }
        System.out.println("total garbage collected " + total + " objects");
        System.out.println("-----\n");
    }

    public void weak() {
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        Map<Integer, WeakEntry> cache = new HashMap<>();
        WeakEntry ref;
        long total = 0;

        System.out.println("# WeakReference example #");
        for (int i=0;i<10000;i++) {
            cache.put(i, new WeakEntry(i, new BigObject(i), queue));
            long count = 0;
            while ((ref = (WeakEntry) queue.poll()) != null) {
                if (count == 0) System.out.print(i + ": garbage collected ");
                System.out.print(ref.getId() + " ");
                count ++;
            }
            if (count > 0) System.out.println();
            total += count;
        }
        System.out.println("total garbage collected " + total + " objects");
        System.out.println("-----\n");
    }

    public static void main(String[] args) {
        new ReferenceExamples().soft();
        new ReferenceExamples().weak();
    }
}

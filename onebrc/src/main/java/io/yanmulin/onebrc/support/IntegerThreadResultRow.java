package io.yanmulin.onebrc.support;

import sun.misc.Unsafe;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;

/**
 * @author Lyuwen Yan
 * @date
 */
public class IntegerThreadResultRow {
    public String city;
    public int min = Integer.MAX_VALUE;
    public int max = Integer.MIN_VALUE;
    public int sum = 0;
    public int count = 0;

    public IntegerThreadResultRow(String city) {
        this.city = city;
    }

    public static IntegerThreadResultRow of(HashThreadResultRow row, MemorySegment segment) {
        MemorySegment slice = segment.asSlice(row.cityOffset, row.cityLength);
        String city = new String(slice.toArray(ValueLayout.JAVA_BYTE));
        IntegerThreadResultRow newRow = new IntegerThreadResultRow(city);
        newRow.min = row.min;
        newRow.max = row.max;
        newRow.sum = row.sum;
        newRow.count = row.count;
        return newRow;
    }

    public static IntegerThreadResultRow of(HashThreadResultRow row, Unsafe unsafe) {
        ByteBuffer buf = ByteBuffer.allocate((int) row.cityLength);
        for (long i=0;i<row.cityLength;i++) {
            buf.put(unsafe.getByte(row.cityOffset + i));
        }
        String city = new String(buf.array());
        IntegerThreadResultRow newRow = new IntegerThreadResultRow(city);
        newRow.min = row.min;
        newRow.max = row.max;
        newRow.sum = row.sum;
        newRow.count = row.count;
        return newRow;
    }
}

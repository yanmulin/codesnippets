package io.yanmulin.onebrc.support;


/**
 * @author Lyuwen Yan
 * @date
 */
public class HashThreadResultRow {
    public int hash;
    public long cityOffset;
    public long cityLength;
    public int min = Integer.MAX_VALUE;
    public int max = Integer.MIN_VALUE;
    public int sum = 0;
    public int count = 0;

    public HashThreadResultRow(int hash, long cityOffset, long cityLength) {
        this.hash = hash;
        this.cityOffset = cityOffset;
        this.cityLength = cityLength;
    }
}

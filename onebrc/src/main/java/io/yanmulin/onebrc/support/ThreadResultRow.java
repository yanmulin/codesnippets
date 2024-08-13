package io.yanmulin.onebrc.support;

/**
 * @author Lyuwen Yan
 * @date
 */
public class ThreadResultRow {
    public String city;
    public float min = Float.MAX_VALUE;
    public float max = Float.MIN_VALUE;
    public float sum = 0f;
    public int count = 0;

    public ThreadResultRow(String city) {
        this.city = city;
    }
}

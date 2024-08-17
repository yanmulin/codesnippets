package io.yanmulin.onebrc.support;

import io.yanmulin.onebrc.Baseline;

/**
 * @author Lyuwen Yan
 * @date
 */
public record Measurement(String city, float temperature) {
    public static Measurement of(String line) {
        String[] comps = line.split(";");
        return new Measurement(comps[0], Float.parseFloat(comps[1]));
    }
}
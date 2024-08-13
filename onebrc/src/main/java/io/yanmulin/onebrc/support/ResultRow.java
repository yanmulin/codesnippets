package io.yanmulin.onebrc.support;

/**
 * @author Lyuwen Yan
 * @date
 */
public record ResultRow(float min, float mean, float max) {
    @Override
    public String toString() {
        return "%.1f/%.1f/%.1f".formatted(min, mean, max);
    }
}
package io.yanmulin.codesnippets.examples.basic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MapExamples {
    public void hasMapEntry() {
        Map<String, String> m = new HashMap<>();
        m.put("aaa", "123");
        m.put("bbb", "456");
        m.put("ccc", "789");

        Map.Entry<String, String> entry = m.entrySet().stream()
                .filter(e -> e.getKey().equals("aaa"))
                .collect(Collectors.toList()).get(0);
        assert entry.getValue().equals("123");

        m.put("aaa", "321");
        assert m.get("aaa").equals("321");
        assert entry.getValue().equals("321"); // entry updated
    }

    public void concurrentHasMapEntry() {
        Map<String, String> m = new ConcurrentHashMap<>();
        m.put("aaa", "123");
        m.put("bbb", "456");
        m.put("ccc", "789");

        Map.Entry<String, String> entry = m.entrySet().stream()
                .filter(e -> e.getKey().equals("aaa"))
                .collect(Collectors.toList()).get(0);
        assert entry.getValue().equals("123");

        m.put("aaa", "321");
        assert m.get("aaa").equals("321");
        assert entry.getValue().equals("123"); // entry not updated
    }

    public void linkedHashMapLRU() {
        final int SIZE = 3;
        Map<String, String> m = new LinkedHashMap<>(SIZE, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> entry) {
                return size() > SIZE;
            }
        };

        m.put("aaa", "123");
        m.put("bbb", "456");
        m.put("ccc", "789");
        m.put("ddd", "000");
        assert !m.containsKey("aaa");
        m.get("bbb");
        m.put("eee", "111");
        assert m.containsKey("bbb");
        assert !m.containsKey("ccc");
    }

    public static void main(String[] args) {
        new MapExamples().concurrentHasMapEntry();
        new MapExamples().hasMapEntry();
        new MapExamples().linkedHashMapLRU();
    }
}

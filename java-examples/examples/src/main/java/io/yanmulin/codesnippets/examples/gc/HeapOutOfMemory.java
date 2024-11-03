package io.yanmulin.codesnippets.examples.gc;

import java.util.ArrayList;
import java.util.List;

public class HeapOutOfMemory {
    private class People {
        String name;
        Integer age;
        String job;
        String sex;
    }

    public void addObjects() {
        List<People> people = new ArrayList<>();
        while (true) {
            people.add(new People());
        }
    }

    public static void main(String[] args) {
        new HeapOutOfMemory().addObjects();
    }
}

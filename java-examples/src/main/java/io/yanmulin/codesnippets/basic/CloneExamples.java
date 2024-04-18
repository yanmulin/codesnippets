package io.yanmulin.codesnippets.basic;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

public class CloneExamples {
    @AllArgsConstructor
    private static class Student {
        String name;
        int age;
    }

    private void arrayListExample() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("123");
        strings.add("456");
        strings.add("789");

        Object c1 = strings.clone();
        assert c1 != strings;
        assert c1.equals(strings);
        assert strings.get(0) == (((ArrayList) c1).get(0));
        assert strings.get(0).equals(((ArrayList) c1).get(0));

        // swallow copy
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student("John", 12));
        students.add(new Student("Alice", 11));
        students.add(new Student("Tom", 14));

        Object c2 = students.clone();
        assert c2 != students;
        assert c2.equals(students);
        assert students.get(0) == (((ArrayList) c2).get(0));

    }

    public static void main(String[] args) {
        new CloneExamples().arrayListExample();
    }
}

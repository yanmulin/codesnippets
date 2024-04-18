package io.yanmulin.codesnippets.patterns.builder;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public class SimpleBuilder {
    public static class Builder {
        private Student student = new Student();
        public Builder name(String name) {
            student.name = name;
            return this;
        }

        public Builder age(int age) {
            student.age = age;
            return this;
        }

        public Builder addClass(String name) {
            student.classes.add(name);
            return this;
        }

        public Student build() { return student; }
    }

    @ToString
    private static class Student {
        String name;
        int age;
        List<String> classes = new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println(new Builder().name("John").age(10).addClass("Math")
                .addClass("English").build());
    }
}

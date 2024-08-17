package io.yanmulin.codesnippets.spring.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;

@Getter
public class Person {
    @Id Long id;

    String name;
}

package io.yanmulin.codesnippets.spring.model;

import lombok.Getter;
import org.springframework.data.annotation.Transient;

@Getter
public class Toy {

    String name;

    @Transient
    Minion minion;

    Toy(String name) {
        this.name = name;
    }
}

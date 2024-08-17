package io.yanmulin.codesnippets.spring.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.*;

@Getter
public class Minion {
    @Id Long id;

    String name;

    AggregateReference<Person, Long> master;

    final Set<Toy> toys = new HashSet<>();

    final Set<Friendship> friends = new HashSet();

    Minion(String name) {
        this.name = name;
    }

    @PersistenceCreator
    private Minion(Long id, String name, Collection<Toy> toys, Collection<Friendship> friends) {

        this.id = id;
        this.name = name;
        this.friends.addAll(friends);
        toys.forEach(this::addToy);
    }

    public void addToy(Toy toy) {
        toys.add(toy);
        toy.minion = this;
    }

}

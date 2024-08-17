package io.yanmulin.codesnippets.spring;

import io.yanmulin.codesnippets.spring.model.Friendship;
import io.yanmulin.codesnippets.spring.model.Minion;
import io.yanmulin.codesnippets.spring.model.Person;
import io.yanmulin.codesnippets.spring.model.Toy;
import io.yanmulin.codesnippets.spring.repository.MinionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import java.util.Optional;

@DataJdbcTest
public class RepositoryTests {
    @Autowired
    MinionRepository minionRepository;
    @Autowired
    JdbcAggregateTemplate jdbcAggregateTemplate;

    @Test
    public void internalReference() {
        Optional<Minion> minion = minionRepository.findById(1L);
        Assertions.assertTrue(minion.isPresent());
        Assertions.assertEquals(1, minion.get().getToys().size());

        Toy toy = minion.get().getToys().iterator().next();
        Assertions.assertEquals("Teddy Bear", toy.getName());

        minionRepository.delete(minion.get());
        minion = minionRepository.findById(1L);
        Assertions.assertFalse(minion.isPresent());
        Assertions.assertEquals(0, jdbcAggregateTemplate.count(Toy.class));
    }

    @Test
    public void externalReference() {
        Optional<Minion> minion = minionRepository.findByMaster(1L);
        Assertions.assertTrue(minion.isPresent());
        Assertions.assertEquals("Kevin", minion.get().getName());

        minionRepository.delete(minion.get());
        minion = minionRepository.findById(minion.get().getId());
        Assertions.assertFalse(minion.isPresent());
        Assertions.assertEquals(1, jdbcAggregateTemplate.count(Person.class));
    }

    @Test
    public void manyToMany() {
        Optional<Minion> minion = minionRepository.findById(3L);
        Assertions.assertTrue(minion.isPresent());
        Assertions.assertEquals(2, minion.get().getFriends().size());

        minionRepository.delete(minion.get());
        minion = minionRepository.findById(minion.get().getId());
        Assertions.assertFalse(minion.isPresent());
        Assertions.assertEquals(0, jdbcAggregateTemplate.count(Friendship.class));
    }
}

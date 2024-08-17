package io.yanmulin.codesnippets.spring.repository;

import io.yanmulin.codesnippets.spring.model.Minion;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MinionRepository extends CrudRepository<Minion, Long> {
    Optional<Minion> findByMaster(Long master);
}

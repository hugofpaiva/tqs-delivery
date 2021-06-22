package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Generic;

import java.util.Optional;

@Repository
public interface GenericRepository extends JpaRepository<Generic, Long> {
    Optional<Generic> findByToken(String token);
}

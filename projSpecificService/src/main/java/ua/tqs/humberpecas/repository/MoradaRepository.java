package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Morada;

@Repository
public interface MoradaRepository extends JpaRepository<Morada, Long> {
}

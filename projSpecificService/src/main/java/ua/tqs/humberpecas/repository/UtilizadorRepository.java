package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Utilizador;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
}

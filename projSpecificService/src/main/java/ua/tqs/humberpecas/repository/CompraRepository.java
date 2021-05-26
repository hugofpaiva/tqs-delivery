package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
}

package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Encomenda;

@Repository
public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {
}

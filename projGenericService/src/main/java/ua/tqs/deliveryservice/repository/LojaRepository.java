package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.tqs.deliveryservice.model.Encomenda;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {
}

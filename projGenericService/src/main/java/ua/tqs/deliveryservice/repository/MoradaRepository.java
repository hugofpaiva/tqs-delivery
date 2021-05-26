package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Morada;

@Repository
public interface MoradaRepository extends JpaRepository<Morada, Long> {
}

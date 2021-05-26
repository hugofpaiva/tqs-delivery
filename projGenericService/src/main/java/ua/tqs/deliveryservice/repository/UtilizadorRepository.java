package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.tqs.deliveryservice.model.Loja;
import ua.tqs.deliveryservice.model.Morada;

public interface MoradaRepository extends JpaRepository<Morada, Long> {
}

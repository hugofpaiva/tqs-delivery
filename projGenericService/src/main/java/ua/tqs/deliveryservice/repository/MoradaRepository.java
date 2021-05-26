package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.tqs.deliveryservice.model.Encomenda;
import ua.tqs.deliveryservice.model.Loja;

public interface LojaRepository extends JpaRepository<Loja, Long> {
}

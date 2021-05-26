package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Loja;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {
}

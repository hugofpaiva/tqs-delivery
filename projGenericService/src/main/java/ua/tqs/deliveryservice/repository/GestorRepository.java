package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Gestor;

@Repository
public interface GestorRepository extends JpaRepository<Gestor, Long> {
}

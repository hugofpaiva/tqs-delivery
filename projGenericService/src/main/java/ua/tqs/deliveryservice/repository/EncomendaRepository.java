package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.tqs.deliveryservice.model.Condutor;

public interface CondutorRepository extends JpaRepository<Condutor, Long> {
}

package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Condutor;

@Repository
public interface CondutorRepository extends JpaRepository<Condutor, Long> {
}

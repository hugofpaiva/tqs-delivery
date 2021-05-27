package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Rider;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
}

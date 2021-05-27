package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
}

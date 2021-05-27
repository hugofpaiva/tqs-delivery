package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}

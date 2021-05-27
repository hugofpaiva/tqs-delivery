package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
}

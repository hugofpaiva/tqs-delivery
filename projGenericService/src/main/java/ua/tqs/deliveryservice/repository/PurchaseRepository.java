package ua.tqs.deliveryservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findAllByRider(Rider rider, Pageable pageable);
}

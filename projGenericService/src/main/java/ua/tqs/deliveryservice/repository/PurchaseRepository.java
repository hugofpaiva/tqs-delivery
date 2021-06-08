package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Purchase findTopByRiderIsNullOrderByDate();
    Purchase findTopByRiderAndStatusIsNot(Rider r, Status s);
}

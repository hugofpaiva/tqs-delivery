package ua.tqs.deliveryservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.model.Store;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findTopByRiderIsNullOrderByDate();
    Optional<Purchase> findTopByRiderAndStatusIsNot(Rider r, Status s);
    Page<Purchase> findAllByRider(Rider rider, Pageable pageable);

    int countPurchaseByStatusIsNot(Status s);

    Optional<Purchase> findTopByOrderByDate();

    Long countPurchaseByStore(Store store);

}

package ua.tqs.deliveryservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.model.Store;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findTopByRiderIsNullOrderByDate();
    Optional<Purchase> findTopByRiderAndStatusIsNot(Rider r, Status s);
    Page<Purchase> findAllByRider(Rider rider, Pageable pageable);

    Optional<Purchase> findTopByOrderByDate();

    Long countPurchaseByStore(Store store);
    Long countPurchaseByStatusIs(Status status);
    Long countPurchaseByStatusIsNot(Status status); // todo: not tested

    Page<Purchase> findAllByRiderIsNullOrderByDate(Pageable pageable); // todo: not tested


    @Query("SELECT SUM(p.deliveryTime), COUNT(p) FROM Purchase p WHERE p.status = 'DELIVERED'")
    List<Long[]> getSumDeliveryTimeAndCountPurchases();

    @Query(value = "SELECT a.city, COUNT(a.city) FROM Purchase p JOIN p.address a " +
            "GROUP BY p.address.city ORDER BY count(a.city) DESC")
    List<Object[]> getTopFiveCitiesOfPurchases();

}

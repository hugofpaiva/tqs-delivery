package ua.tqs.humberpecas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.model.Purchase;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByServiceOrderId(Long serviceOrderId);
    Page<Purchase> findAllByPerson(Person person, Pageable pageable);
    int countPurchaseByPersonAndRiderReviewNotNull(Person person);

}

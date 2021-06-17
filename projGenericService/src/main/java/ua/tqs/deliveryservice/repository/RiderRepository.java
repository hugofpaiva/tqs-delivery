package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Rider;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Rider> findByEmail(String email);

    @Query("SELECT SUM(r.reviewsSum), SUM(r.totalNumReviews) FROM  Rider r")
    List<Long[]> getSumReviewsAndQuantity();

}

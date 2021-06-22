package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Rider;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Rider> findByEmail(String email);

    @Query("SELECT AVG(1.0*r.reviewsSum/r.totalNumReviews) FROM  Rider r WHERE r.totalNumReviews <> 0")
    Double getAverageRiderRating();
}

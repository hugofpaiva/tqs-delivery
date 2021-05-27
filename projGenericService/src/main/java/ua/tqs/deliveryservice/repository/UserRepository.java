package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

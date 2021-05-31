package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}

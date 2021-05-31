package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}

package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByPersonAndDeletedIsFalse(Person person);
}

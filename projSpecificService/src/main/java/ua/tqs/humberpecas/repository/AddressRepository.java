package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Address;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    //TODO: testar este metodo
    Optional<List<Address>> findByPersonId(long personId);
}

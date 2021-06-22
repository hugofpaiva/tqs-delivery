package ua.tqs.deliveryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.deliveryservice.model.Address;


@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

}

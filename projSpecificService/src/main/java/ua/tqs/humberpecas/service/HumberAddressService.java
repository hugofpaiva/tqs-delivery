package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.List;

@Log4j2
@Service
public class HumberAddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Address addNewAddress(String token, AddressDTO addressDTO) throws InvalidLoginException {
        Person p = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(token))
                .orElseThrow(() -> {
                            log.error("HUMBER ADDRESS SERVICE: Invalid User, when adding new address");
                            return new InvalidLoginException("Invalid User"); });

        var newAddress = new Address(addressDTO.getAddress(), addressDTO.getPostalCode(), addressDTO.getCity(), addressDTO.getCountry(), p);

        return addressRepository.save(newAddress);
    }

    public Address delAddress(String token, long addressId) throws InvalidLoginException {
        Person p = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(token))
                .orElseThrow(() -> {
                    log.error("HUMBER ADDRESS SERVICE: Invalid User, when deleting address");
                    return new InvalidLoginException("Invalid User"); });

        var address = addressRepository.findById(addressId)
                .orElseThrow(() ->  {
                    log.error("HUMBER ADDRESS SERVICE: Invalid address, when deleting address");
                            return new ResourceNotFoundException("Invalid Address"); });

        if (!p.getAddresses().contains(address)) {
            log.error("HUMBER ADDRESS SERVICE: Address does not belong to this user, when deleting address");
            throw new ResourceNotFoundException("Address to be deleted does not belong to this user.");
        }

        address.setDeleted(true);

        log.info("HUMBER ADDRESS SERVICE: Successfully deleted address");
        return addressRepository.saveAndFlush(address);
    }

    public List<Address> getUserAddress(String userToken) throws InvalidLoginException {

        Person person = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(userToken))
                .orElseThrow(()-> {
                    log.error("HUMBER ADDRESS SERVICE: Invalid user token, when getting user address" );
                    return new InvalidLoginException("Invalid user token");
                });

        log.info("HUMBER ADDRESS SERVICE: Successfully got user address");
        return addressRepository.findAllByPersonAndDeletedIsFalse(person);
    }


}

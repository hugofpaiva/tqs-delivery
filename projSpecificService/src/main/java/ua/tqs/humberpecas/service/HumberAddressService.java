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
                    log.error("Invalid User");
                    return new InvalidLoginException("Invalid User"); });

        var newAddress = new Address(addressDTO.getAddress(), addressDTO.getPostalCode(), addressDTO.getCity(), addressDTO.getCountry());
        newAddress.setPerson(p);

        return addressRepository.save(newAddress);
    }

    public void delAddress(String token, long addressId) throws InvalidLoginException {
        Person p = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(token))
                .orElseThrow(() -> {
                    log.error("Invalid User");
                    return new InvalidLoginException("Invalid User"); });

        var address = addressRepository.findById(addressId)
                .orElseThrow(() ->  {
                            log.error("Invalid Address");
                            return new ResourceNotFoundException("Invalid Address"); });

        if (!p.getAddresses().contains(address)) throw new ResourceNotFoundException("Address to be deleted does not belong to this user.");

        addressRepository.delete(address);
    }

    public List<Address> getUserAddress(String userToken) throws InvalidLoginException {

        Person person = personRepository.findByEmail(jwtUserDetailsService.getEmailFromToken(userToken))
                .orElseThrow(()-> {
                    log.error("HumberPurchaseService: invalid user token" );
                    return new InvalidLoginException("Invalid user token");
                });

        List<Address> addresses = addressRepository.findAllByPerson(person);

        return addresses;
    }


}

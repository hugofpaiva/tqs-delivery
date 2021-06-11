package ua.tqs.humberpecas.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.List;


@Service
public class HumberAddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PersonRepository personRepository;

    public Address addNewAddress(AddressDTO addressDTO) throws ResourceNotFoundException {

        Person p = personRepository.findById(addressDTO.getPersonID())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid User"));

        // TODO: verificar ser o address ja existe

        Address newAddress = new Address(addressDTO.getAddress(), addressDTO.getPostalCode(), addressDTO.getCity(), addressDTO.getCountry(), p);

        return addressRepository.save(newAddress);
    }

    public Address updateAddress(AddressDTO addressDTO) throws ResourceNotFoundException {

        Address address = addressRepository.findById(addressDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Address"));

        address.setAddress(addressDTO.getAddress());
        address.setCity(addressDTO.getCity());
        address.setCountry(addressDTO.getCountry());
        address.setPostalCode(addressDTO.getPostalCode());

        return addressRepository.save(address);

    }

    public void delAddress(AddressDTO addressDTO) throws ResourceNotFoundException {

        Address address = addressRepository.findById(addressDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Address"));

        addressRepository.delete(address);

    }

    public List<Address> getUserAddress(long userId) throws ResourceNotFoundException {

        List<Address> addresses = addressRepository.findByPersonId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Person"));

        return addresses;

    }


}

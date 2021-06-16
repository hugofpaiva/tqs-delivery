package ua.tqs.humberpecas.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ua.tqs.humberpecas.service.HumberAddressService;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HumberAddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PersonRepository personRepository;


    @InjectMocks
    private HumberAddressService service;

    private Address  address;
    private AddressDTO addressDTO;
    private Person person;

    @BeforeEach
    void setUp() throws IOException {
        person = new Person("Fernando", "12345678","fernando@ua.pt");
        address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);
        addressDTO = new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal");
    }


    // -------------------------------------
    // --       ADD ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Add new Address")
    void whenUserAddAddress_thenSaveAddress() throws ResourceNotFoundException {
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(person));
        when(addressRepository.save(address)).thenReturn(address);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        Address newAddress = service.addNewAddress("token", addressDTO);


        assertThat(newAddress.getAddress(), equalTo(address.getAddress()));
        assertThat(newAddress.getCity(), equalTo(address.getCity()));
        assertThat(newAddress.getCountry(), equalTo(address.getCountry()));
        assertThat(newAddress.getPostalCode(), equalTo(address.getPostalCode()));
        assertThat(newAddress.getPerson(), equalTo(address.getPerson()));

        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).save(address);

    }

    @Test
    @DisplayName("Add new Address of Invalid User throws ResourceNotFoundException")
    void whenInvalidUserAddAddress_thenThrowsResourceNotFound() throws ResourceNotFoundException {

        addressDTO.setPersonID(1L);

        assertThrows( ResourceNotFoundException.class, () -> {
            service.addNewAddress("token", addressDTO);
        } );

        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(0)).save(address);

    }

    // -------------------------------------
    // --       GET ADDRESS TESTS         --
    // -------------------------------------
/*
    @Test
    @DisplayName("Get User Addresses")
    void whenGetValidUser_thenReturnAddress() throws InvalidLoginException {
        Address address2 = new Address("Coimbra", "3730-125","Coimbra","Portugal", person);
        List<Address> addresses = Arrays.asList(address, address2);

        when(addressRepository.findByPerson(any())).thenReturn(addresses);

        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));

        when(jwtUserDetailsService.getEmailFromToken(any())).thenReturn(person.getEmail());

        List<Address> userAddresses = service.getUserAddress("Token");

        assertThat(userAddresses, hasSize(2));
        assertThat(userAddresses, hasItem(address));
        assertThat(userAddresses, hasItem(address2));

        verify(addressRepository, times(1)).findByPerson(person);

    }

    @Test
    @DisplayName("Get Addresses of Invalid User throws InvalidLoginException")
    void whenGetAddressesInvalidUser_thenThrowInvalidLoginException() {

        when(personRepository.findByEmail(any())).thenReturn(Optional.empty());

        when(jwtUserDetailsService.getEmailFromToken(any())).thenReturn("Invalidemail@email.com");

        assertThrows( InvalidLoginException.class, () -> {
            service.getUserAddress("Token");
        } );

        verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());

        verify(personRepository, times(1)).findByEmail(any());

    }

    // -------------------------------------
    // --       DELETE ADDRESS TESTS      --
    // -------------------------------------

    @Test
    @DisplayName("Delete User Address")
    void whenDeleteValidAddress_thenReturnNothing() throws ResourceNotFoundException {
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));

        addressDTO.setAddressId(1L);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(address);

        service.delAddress("sometoken", addressDTO);

        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).delete(address);

    }

    @Test
    @DisplayName("Delete Invalid Address throws ResourceNotFoundException")
    void whenDeleteInvalidAddress_thenThrowResourceNotFound() throws ResourceNotFoundException {
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
        addressDTO.setAddressId(1L);

        assertThrows( ResourceNotFoundException.class, () -> {
            service.delAddress("sometoken", addressDTO);
        } );

        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(0)).delete(address);
    }

    @Test
    @DisplayName("Delete address mismatched from person throws ResourceNotFoundException")
    void whenDeleteButAddressDoesntBelongToPerson_thenThrowResourceNotFound() throws ResourceNotFoundException {
        Person other_person = new Person("Duarte", "strong!password","duarte@ua.pt");
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(other_person));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        assertThrows( ResourceNotFoundException.class, () -> {
            service.delAddress("sometoken", addressDTO);
        });

        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(address.getId());
        verify(addressRepository, times(0)).delete(address);
    }


 */

}

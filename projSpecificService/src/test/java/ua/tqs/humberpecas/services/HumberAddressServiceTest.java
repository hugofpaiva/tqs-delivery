package ua.tqs.humberpecas.services;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        addressDTO = new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal", 1);

    }


    @Test
    @DisplayName("Add new Address")
    void whenUserAddAddress_thenSaveAddress() throws ResourceNotFoundException {


        when(personRepository.findById(anyLong())).thenReturn(Optional.of(person));
        when(addressRepository.save(address)).thenReturn(address);

        Address newAddress = service.addNewAddress(addressDTO);


        assertThat(newAddress.getAddress(), equalTo(address.getAddress()));
        assertThat(newAddress.getCity(), equalTo(address.getCity()));
        assertThat(newAddress.getCountry(), equalTo(address.getCountry()));
        assertThat(newAddress.getPostalCode(), equalTo(address.getPostalCode()));
        assertThat(newAddress.getPerson(), equalTo(address.getPerson()));

        verify(personRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).save(address);

    }

    @Test
    @DisplayName("Add new Address of Invalid User throws ResourceNotFoundException")
    void whenInvalidUserAddAddress_thenThrowsResourceNotFound() throws ResourceNotFoundException {


        assertThrows( ResourceNotFoundException.class, () -> {
            service.addNewAddress(addressDTO);
        } );

        verify(personRepository, times(1)).findById(1L);
        verify(addressRepository, times(0)).save(address);

    }

    @Test
    @DisplayName("Update User Address")
    void whenUserUpdateValidAddress_thenReturnAddress() throws ResourceNotFoundException {

        addressDTO.setAddressId(1L);
        Address old = new Address("Coimbra", "3730-125","Coimbra","Portugal", person);

        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(old));
        when(addressRepository.save(address)).thenReturn(address);

        Address newAddress = service.updateAddress(addressDTO);

        assertThat(newAddress.getAddress(), equalTo(address.getAddress()));
        assertThat(newAddress.getCity(), equalTo(address.getCity()));
        assertThat(newAddress.getCountry(), equalTo(address.getCountry()));
        assertThat(newAddress.getPostalCode(), equalTo(address.getPostalCode()));
        assertThat(newAddress.getPerson(), equalTo(address.getPerson()));


        verify(addressRepository, times(1)).findById(anyLong());
        verify(addressRepository, times(1)).save(address);


    }

    @Test
    @DisplayName("Update Invalid User Address throws ResourceNotFoundException")
    void whenUserUpdateInvalidAddress_thenThrowsResourceNotFound() throws ResourceNotFoundException {
        addressDTO.setAddressId(1L);

        assertThrows( ResourceNotFoundException.class, () -> {
            service.updateAddress(addressDTO);
        } );

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(0)).save(address);

    }


    @Test
    @DisplayName("Get User Addresses")
    void whenGetValidUser_thenReturnAddress() throws ResourceNotFoundException {

        Address address2 = new Address("Coimbra", "3730-125","Coimbra","Portugal", person);
        List<Address> addresses = Arrays.asList(address, address2);

        when(addressRepository.findByPersonId(anyLong())).thenReturn(Optional.of(addresses));

        List<Address> userAddresses = service.getUserAddress(1L);

        assertThat(userAddresses, hasSize(2));
        assertThat(userAddresses, hasItem(address));
        assertThat(userAddresses, hasItem(address2));

        verify(addressRepository, times(1)).findByPersonId(1L);

    }

    @Test
    @DisplayName("Get Addresses of Invalid User throws ResourceNotFoundException")
    void whenGetAddressesInvalidUser_thenThrowResourceNotFound() throws ResourceNotFoundException {

        assertThrows( ResourceNotFoundException.class, () -> {
            service.getUserAddress(1L);
        } );

        verify(addressRepository, times(1)).findByPersonId(1L);

    }

    @Test
    @DisplayName("Delete User Address")
    void whenDeleteValidAddress_thenReturnNothing() throws ResourceNotFoundException {

        addressDTO.setAddressId(1L);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(address);

        service.delAddress(addressDTO);

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).delete(address);


    }

    @Test
    @DisplayName("Delete Invalid Address throws ResourceNotFoundException")
    void whenDeleteInvalidAddress_thenThrowResourceNotFound() throws ResourceNotFoundException {

        addressDTO.setAddressId(1L);

        assertThrows( ResourceNotFoundException.class, () -> {
            service.delAddress(addressDTO);
        } );

        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(0)).delete(address);

    }




}

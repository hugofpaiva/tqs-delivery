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

import java.util.*;

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

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @InjectMocks
    private HumberAddressService service;

    private Address  address;
    private AddressDTO addressDTO;
    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person("Fernando", "12345678","fernando@ua.pt");
        address = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);
        addressDTO = new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal");
        person.setAddresses(Set.of(address));
    }


    // -------------------------------------
    // --       ADD ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Add new Address")
    void whenUserAddAddress_thenSaveAddress() throws InvalidLoginException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn(person.getEmail());
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
        when(addressRepository.save(address)).thenReturn(address);

        Address newAddress = service.addNewAddress("token", addressDTO);

        assertThat(newAddress.getAddress(), equalTo(address.getAddress()));
        assertThat(newAddress.getCity(), equalTo(address.getCity()));
        assertThat(newAddress.getCountry(), equalTo(address.getCountry()));
        assertThat(newAddress.getPostalCode(), equalTo(address.getPostalCode()));
        assertThat(newAddress.getPerson(), equalTo(address.getPerson()));

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail(person.getEmail());
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    @DisplayName("Add new Address of Invalid User throws ResourceNotFoundException")
    void whenInvalidUserAddress_thenThrowsResourceNotFound() throws InvalidLoginException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn("Invalidemail@email.com");
        when(personRepository.findByEmail("Invalidemail@email.com")).thenReturn(Optional.empty());

        assertThrows( InvalidLoginException.class, () -> {
            service.addNewAddress("token", addressDTO);
        } );

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail("Invalidemail@email.com");
        verify(addressRepository, times(0)).save(address);
    }

    // -------------------------------------
    // --       GET ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Get User Addresses")
    void whenGetValidUser_thenReturnAddress() throws InvalidLoginException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn(person.getEmail());
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
        when(addressRepository.findAllByPerson(person)).thenReturn(new ArrayList<>(person.getAddresses()));

        List<Address> userAddresses = service.getUserAddress("token");

        assertThat(userAddresses, hasSize(1));
        assertThat(userAddresses, hasItem(address));

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(addressRepository, times(1)).findAllByPerson(person);
        verify(personRepository, times(1)).findByEmail(person.getEmail());
    }

    @Test
    @DisplayName("Get Addresses of Invalid User throws InvalidLoginException")
    void whenGetAddressesInvalidUser_thenThrowInvalidLoginException() throws InvalidLoginException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn("Invalidemail@email.com");
        when(personRepository.findByEmail("Invalidemail@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            service.getUserAddress("token");
        } );

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail("Invalidemail@email.com");

    }

    // -------------------------------------
    // --       DELETE ADDRESS TESTS      --
    // -------------------------------------

    @Test
    @DisplayName("Delete User Address: valid parameters")
    void whenDeleteEverythingIsOk_thenReturnNothing() throws InvalidLoginException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn(person.getEmail());
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        doNothing().when(addressRepository).delete(address);

        service.delAddress("token", 1L);

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).delete(address);
    }

    @Test
    @DisplayName("Delete Address: Invalid AddressId throws ResourceNotFoundException")
    void whenDeleteInvalidAddressId_thenThrowResourceNotFound() throws ResourceNotFoundException {
        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn(person.getEmail());
        when(personRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
        when(addressRepository.findById(-1L)).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> {
            service.delAddress("token", -1L);
        });
        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(-1L);
        verify(addressRepository, times(0)).delete(address);
    }

    @Test
    @DisplayName("Delete Address: Invalid Token throws ResourceNotFoundException")
    void whenDeleteInvalidToken_thenThrowResourceNotFound() {
        when(jwtUserDetailsService.getEmailFromToken("wrong_token")).thenReturn("Invalidemail@email.com");
        when(personRepository.findByEmail("Invalidemail@email.com")).thenReturn(Optional.empty());

        assertThrows( InvalidLoginException.class, () -> {
            service.delAddress("wrong_token", 1L);
        } );
        verify(jwtUserDetailsService, times(1)).getEmailFromToken("wrong_token");
        verify(personRepository, times(1)).findByEmail("Invalidemail@email.com");
        verify(addressRepository, times(0)).findById(anyLong());
        verify(addressRepository, times(0)).delete(address);
    }

    @Test
    @DisplayName("Delete Address: address doesn't belong to person throws ResourceNotFoundException")
    void whenDeleteButAddressDoesntBelongToPerson_thenThrowResourceNotFound() throws ResourceNotFoundException {
        Person other_person = new Person("Duarte", "strong!password","duarte@ua.pt");
        other_person.setAddresses( Set.of(
            new Address("Aveiro", "3730-123","Aveiro","Portugal", other_person)
        ));

        when(jwtUserDetailsService.getEmailFromToken("token")).thenReturn(other_person.getEmail());
        when(personRepository.findByEmail(other_person.getEmail())).thenReturn(Optional.of(other_person));
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThrows( ResourceNotFoundException.class, () -> {
            service.delAddress("token", address.getId());
        });

        verify(jwtUserDetailsService, times(1)).getEmailFromToken("token");
        verify(personRepository, times(1)).findByEmail(anyString());
        verify(addressRepository, times(1)).findById(address.getId());
        verify(addressRepository, times(0)).delete(address);
    }

}

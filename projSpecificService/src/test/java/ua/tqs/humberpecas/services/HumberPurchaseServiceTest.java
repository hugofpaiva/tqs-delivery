package ua.tqs.humberpecas.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import ua.tqs.humberpecas.delivery.IDeliveryService;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.dto.PurchaseDeliveryDTO;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.AddressRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.repository.ProductRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.service.HumberPurchaseService;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class HumberPurchaseServiceTest {

    @Mock
    private IDeliveryService deliveryService;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private HumberPurchaseService purchaseService;


    private Person person;
    private Address address;
    private List<Product> productList;
    private String userToken;
    private PurchaseDeliveryDTO purchaseDeliveryDTO;
    private PurchaseDTO purchaseDTO;
    private Date date;
    private List<Long> productsIds;

    @BeforeEach
    public void setUp(){
        date = new Date();
        person = new Person("Fernando", "12345678","fernando@ua.pt");
        address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);

        productList = Arrays.asList(
                new Product( 10.50 , "hammer", "the best hammer", Category.SCREWDRIVER),
                new Product(20.50 , "hammer v2", "the best hammer 2.0",  Category.SCREWDRIVER));

        this.userToken = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCI6MTYyMzYyMDQzMiwiaWF0IjoxNjIzNjIwNDMyfQ.Gib-gCJyL8-__G3zN4E-9VV1q75eYHZ8X6sS1WUNZB8";


        productsIds = Arrays.asList(3L, 4L);
        purchaseDTO = new PurchaseDTO( date, 2L, productsIds);

        this.purchaseDeliveryDTO = new PurchaseDeliveryDTO(
                person.getName(),
                date,
                new AddressDTO(address.getAddress(), address.getPostalCode(), address.getCity(), address.getCountry())
                );
    }

    @Test
    @DisplayName("Cant communicate with delivery service throws UnreachableServiceExcption")
    void whenErrorInCommunication_thenThrowsStatusUnreachableService(){

        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(productRepository.findAllById(anyList())).thenReturn(productList);

        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        doThrow(UnreachableServiceException.class).when(deliveryService).newOrder(purchaseDeliveryDTO);

        assertThrows( UnreachableServiceException.class, () -> {
            purchaseService.newPurchase(purchaseDTO, userToken);
        } );


        verify(deliveryService, times(1)).newOrder(purchaseDeliveryDTO);
        verify(purchaseRepository, times(0)).saveAndFlush(any());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);
        verify(addressRepository, times(1)).findById(2L);
        verify(productRepository,times(1)).findAllById(productsIds);

    }

    @Test
    @DisplayName("Make Purchase with nonexistent Address throws ResourceNotFoundException")
    void whenUserAddressToken_thenThrowsStatusResourceNotFound(){

        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());

        assertThrows( ResourceNotFoundException.class, () -> {
            purchaseService.newPurchase(purchaseDTO, userToken);
        } );

        verify(deliveryService, times(0)).newOrder(purchaseDeliveryDTO);
        verify(purchaseRepository, times(0)).saveAndFlush(any());
        verify(addressRepository, times(1)).findById(2L);


    }

    @Test
    @DisplayName("Make Purchase where address dont belong to user throws AccessNotAllowedException")
    void whenPurchaseNotBelongAddress_thenThrowsStatusAccessNotAllowed(){

        Person person1 = new Person("Antonio", "12345678","to@ua.pt");

        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person1));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));

        assertThrows( AccessNotAllowedException.class, () -> {
            purchaseService.newPurchase(purchaseDTO, userToken);
        } );


        verify(deliveryService, times(0)).newOrder(purchaseDeliveryDTO);
        verify(purchaseRepository, times(0)).saveAndFlush(any());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);
        verify(addressRepository, times(1)).findById(2L);
        verify(productRepository,times(0)).findAllById(productsIds);

    }

    @Test
    @DisplayName("Make Purchase of invalid Products throws ResourceNotFoundException")
    void whenPurchaseInvalidProducts_thenThrowStatusResourceNotFound(){

        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(productRepository.findAllById(productsIds)).thenReturn(productList.subList(0,0));

        assertThrows( ResourceNotFoundException.class, () -> {
            purchaseService.newPurchase(purchaseDTO, userToken);
        } );

        verify(deliveryService, times(0)).newOrder(purchaseDeliveryDTO);
        verify(purchaseRepository, times(0)).saveAndFlush(any());
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);
        verify(addressRepository, times(1)).findById(2L);
        verify(productRepository,times(1)).findAllById(productsIds);



    }

    @Test
    @DisplayName("Make Purchase")
    void whenValidPurchase_thenReturnPuchase(){


        Purchase p = new Purchase(person, address, productList);
        p.setDate(date);
        p.setServiceOrderId(5L);

        when(jwtUserDetailsService.getEmailFromToken(anyString())).thenReturn(person.getEmail());
        when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(productRepository.findAllById(productsIds)).thenReturn(productList);
        when(deliveryService.newOrder(purchaseDeliveryDTO)).thenReturn(5L);
        when(purchaseRepository.save(p)).thenReturn(p);

        Purchase purchase = purchaseService.newPurchase(purchaseDTO, userToken);

        assertThat(purchase.getPerson(), equalTo(person));
        assertThat(purchase.getServiceOrderId(), equalTo(5L));
        assertThat(purchase.getAddress(), equalTo(address));
        assertThat(purchase.getProducts(), equalTo(productList));
        assertThat(purchase.getProducts(), equalTo(productList));
        assertThat(purchase.getDate(), equalTo(date));

        verify(deliveryService, times(1)).newOrder(purchaseDeliveryDTO);
        verify(purchaseRepository, times(1)).save(p);
        verify(jwtUserDetailsService, times(1)).getEmailFromToken(userToken);
        verify(addressRepository, times(1)).findById(2L);
        verify(productRepository,times(1)).findAllById(productsIds);

    }
}

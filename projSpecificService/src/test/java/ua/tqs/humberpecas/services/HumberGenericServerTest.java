package ua.tqs.humberpecas.services;


import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.*;
import ua.tqs.humberpecas.repository.GenericRepository;
import ua.tqs.humberpecas.repository.PurchaseRepository;
import ua.tqs.humberpecas.service.HumberGenericServer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HumberGenericServerTest {


    @Mock
    private GenericRepository genericRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private HumberGenericServer service;

    private String genericToken;
    private PurchaseStatus purchaseStatus;
    private Purchase purchase;
    private Generic generic;


    @BeforeEach
    void setUp(){

        Person person = new Person("Fernando", "12345678","fernando@ua.pt");
        Address address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);

        List<Product> productList = Arrays.asList(
                new Product( "hammer", 10.50 ,Category.SCREWDRIVER,  "the best hammer", "img.png" ),
                new Product( "hammer v2", 20.50  ,Category.SCREWDRIVER, "the best hammer 2.0", "img.png" ));

        purchase = new Purchase(person, address, productList);
        genericToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
        generic = new Generic("generic", genericToken);
    }



    @Test
    @DisplayName("Update Order status")
    void whenUpdateValidOrder_thenUpdateOrder(){


        when(genericRepository.findByToken(genericToken)).thenReturn(Optional.of(generic));
        when(purchaseRepository.findByServiceOrderId(1L)).thenReturn(Optional.of(purchase));

        service.updateOrderStatus(1L, genericToken, purchaseStatus);

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(1)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(1)).saveAndFlush(purchase);
    }

    @Test
    @DisplayName("Update Order Status with invalid generic token throws InvalidLoginExcetion")
    void whenUpdateOrderInvalidToken_thenThrowInvalidLogin(){

        assertThrows( InvalidLoginException.class, () -> {
            service.updateOrderStatus(1L, genericToken, purchaseStatus);
        } );

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(0)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(0)).saveAndFlush(any());

    }

    @Test
    @DisplayName("Update Order Status of invalid order throws ResourseNotFound")
    void whenUpdateInvalidOrder_thenThrowResourceNotFound(){

        when(genericRepository.findByToken(genericToken)).thenReturn(Optional.of(generic));

        assertThrows( ResourceNotFoundException.class, () -> {
            service.updateOrderStatus(1L, genericToken, purchaseStatus);
        } );

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(1)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(0)).saveAndFlush(any());


    }


    @Test
    @DisplayName("Update Order Status with invalid generic token throws InvalidLoginExcetion")
    void whenSetRiderInvalidToken_thenThrowInvalidLogin(){

        assertThrows( InvalidLoginException.class, () -> {
            service.setRider(1L, genericToken, "Tone");
        } );

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(0)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(0)).saveAndFlush(any());

    }

    @Test
    @DisplayName("Set Rider of invalid order throws ResourseNotFound")
    void whenSetRiderInvalidOrder_thenThrowResourceNotFound(){

        when(genericRepository.findByToken(genericToken)).thenReturn(Optional.of(generic));

        assertThrows( ResourceNotFoundException.class, () -> {
            service.setRider(1L, genericToken, "tone");
        } );

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(1)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(0)).saveAndFlush(any());


    }


    @Test
    @DisplayName("Set Rider in order already accepted throws InvalidOperationException")
    void whenSetRiderAcceptedOrder_thenThrowsInvalidOperation(){

        purchase.setStatus(PurchaseStatus.PICKED_UP);

        when(genericRepository.findByToken(genericToken)).thenReturn(Optional.of(generic));
        when(purchaseRepository.findByServiceOrderId(1L)).thenReturn(Optional.of(purchase));

        assertThrows( InvalidOperationException.class, () -> {
            service.setRider(1L, genericToken, "tone");
        } );

        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(1)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(0)).saveAndFlush(any());

    }

    @Test
    @DisplayName("Set Rider")
    void whenSetRider_thenReturnPurchase(){



        when(genericRepository.findByToken(genericToken)).thenReturn(Optional.of(generic));
        when(purchaseRepository.findByServiceOrderId(1L)).thenReturn(Optional.of(purchase));
        when(purchaseRepository.saveAndFlush(any())).thenReturn(purchase);

        Purchase p = service.setRider(1L, genericToken, "tone");

        assertThat(p.getRiderName(), Matchers.is("tone"));
        assertThat(p.getStatus(), Matchers.equalTo(PurchaseStatus.ACCEPTED));
        assertThat(p.getProducts(), Matchers.equalTo(purchase.getProducts()));
        assertThat(p.getAddress(), Matchers.equalTo(purchase.getAddress()));
        assertThat(p.getPerson(), Matchers.equalTo(purchase.getPerson()));


        verify(genericRepository, times(1)).findByToken(genericToken);
        verify(purchaseRepository, times(1)).findByServiceOrderId(1L);
        verify(purchaseRepository, times(1)).saveAndFlush(purchase);

    }

}

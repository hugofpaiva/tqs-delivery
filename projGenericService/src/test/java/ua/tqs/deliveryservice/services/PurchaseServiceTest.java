package ua.tqs.deliveryservice.services;


import org.assertj.core.data.Percentage;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import ua.tqs.deliveryservice.exception.*;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

import org.springframework.data.domain.*;
import ua.tqs.deliveryservice.specific.ISpecificService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    private Rider rider = new Rider("example", "pwd", "email@email.com");
    private Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
    private Store store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", this.address, "http://localhost:8081/delivery/");
    private Purchase purchase = new Purchase(this.address, this.rider, this.store, "Joana");


    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ISpecificService specificService;


    /* ----------------------------- *
     * UPDATE PURCHASE STATUS        *
     * ----------------------------- *
     */

    @Test
    void testUpdatePurchaseStatusOfRider_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.updatePurchaseStatus("exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    void testUpdatePurchaseStatusOfRider_whenRiderHasNoPurchaseThrows() {
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.updatePurchaseStatus("exampleToken");
        }, "This rider hasn't accepted an order yet");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(0))
                .updateOrderStatus(any(),anyString());
    }

    @Test
    void testUpdateCurrentPurchaseOfRiderValid() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel"); p1.setDate(new Date());
        p1.setId(5L);


        Mockito
                .when(jwtUserDetailsService.getEmailFromToken("tokenExample"))
                .thenReturn(rider.getEmail());
        Mockito
                .when(riderRepository.findByEmail(any()))
                .thenReturn(Optional.of(rider));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED))
                .thenReturn(Optional.of(p1));


        // test ...
        Purchase current = purchaseService.updatePurchaseStatus("tokenExample");
        assertThat(current).isEqualTo(p1);
        assertThat(current.getStatus()).isEqualTo(Status.PICKED_UP);
        assertThat(current.getRider()).isEqualTo(r1);
        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).updateOrderStatus(any(),anyString());
    }

    @Test
    public void testUpdateCurrentPurchaseOfRiderValid_whenErrorConnection() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel"); p1.setDate(new Date());
        p1.setId(5L);


        Mockito
                .when(jwtUserDetailsService.getEmailFromToken("tokenExample"))
                .thenReturn(rider.getEmail());
        Mockito
                .when(riderRepository.findByEmail(any()))
                .thenReturn(Optional.of(rider));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED))
                .thenReturn(Optional.of(p1));

        doThrow(UnreachableServiceException.class).when(specificService).updateOrderStatus(any(), anyString());


        // test ...

        Assert.assertThrows( UnreachableServiceException.class, () -> {
            purchaseService.updatePurchaseStatus("tokenExample");
        } );

        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).updateOrderStatus(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }



    @Test
    public void testUpdateCurrentPurchase_whenSendInvalidData() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel"); p1.setDate(new Date());
        p1.setId(5L);


        Mockito
                .when(jwtUserDetailsService.getEmailFromToken("tokenExample"))
                .thenReturn(rider.getEmail());
        Mockito
                .when(riderRepository.findByEmail(any()))
                .thenReturn(Optional.of(rider));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED))
                .thenReturn(Optional.of(p1));

        doThrow(InvalidValueException.class).when(specificService).updateOrderStatus(any(), anyString());


        // test ...

        Assert.assertThrows( InvalidValueException.class, () -> {
            purchaseService.updatePurchaseStatus("tokenExample");
        } );

        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).updateOrderStatus(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }



    @Test
    void testUpdateCurrentPurchaseOfRiderValid_whenStatusWasPICKED_UP_thenVerifyDeliveryTime() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel"); p1.setDate(new Date()); p1.setStatus(Status.PICKED_UP);

        Mockito
                .when(jwtUserDetailsService.getEmailFromToken("tokenExample"))
                .thenReturn(rider.getEmail());
        Mockito
                .when(riderRepository.findByEmail(any()))
                .thenReturn(Optional.of(rider));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED))
                .thenReturn(Optional.of(p1));



        // test ...
        Purchase current = purchaseService.updatePurchaseStatus("tokenExample");
        assertThat(current).isEqualTo(p1);
        assertThat(current.getStatus()).isEqualTo(Status.DELIVERED);
        assertThat(current.getRider()).isEqualTo(r1);
        assertThat(current.getDeliveryTime()).isCloseTo(15L, Percentage.withPercentage(200));
        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).updateOrderStatus(any(),anyString());
    }

    /* ----------------------------- *
     * GET CURRENT PURCHASE TESTS    *
     * ----------------------------- *
     */

    @Test
    void testGetCurrentPurchaseOfRider_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getCurrentPurchase("exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
    }


    @Test
    void testGetCurrentPurchaseOfRider_whenRiderHasNoOrderThrows() {
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.getCurrentPurchase("exampleToken");
        }, "This rider hasn't accepted an order yet");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
    }


    @Test
    void testGetCurrentPurchaseOfRiderValid() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel");

        Mockito
                .when(jwtUserDetailsService.getEmailFromToken("tokenExample"))
                .thenReturn(rider.getEmail());
        Mockito
                .when(riderRepository.findByEmail(any()))
                .thenReturn(Optional.of(rider));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED))
                .thenReturn(Optional.of(p1));

        // test ...
        Purchase current = purchaseService.getCurrentPurchase("tokenExample");
        assertThat(current).isEqualTo(p1);
        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
    }



    /* ----------------------------- *
     * GET NEW PURCHASE TESTS        *
     * ----------------------------- *
     */

    @Test
    void testGetNewPurchaseForRider_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getNewPurchase("exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    void testGetNewPurchaseForRider_whenRiderHasAnOrderAlreadyThrows() {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.of(p1));

        assertThrows(ForbiddenRequestException.class, () -> {
            purchaseService.getNewPurchase("exampleToken");
        }, "This rider still has an order to deliver");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(0)).setRiderName(anyString(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    void testGetNewPurchaseForRider_whenThereIsNoMorePurchases() {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findTopByRiderIsNullOrderByDate())
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.getNewPurchase("exampleToken");
        }, "There are no more orders available");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderIsNullOrderByDate();
        Mockito.verify(specificService, times(0)).setRiderName(anyString(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    public void testGetNewPurchaseForRider_whenErrorConnection() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, store, "Miguel");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findTopByRiderIsNullOrderByDate())
                .thenReturn(Optional.of(p1));


        doThrow(UnreachableServiceException.class).when(specificService).setRiderName(any(), anyString());

        Assert.assertThrows( UnreachableServiceException.class, () -> {
            purchaseService.getNewPurchase("exampleToken");
        } );
        // test ...

            Mockito.verify(jwtUserDetailsService, times(1))
                    .getEmailFromToken("exampleToken");
            Mockito.verify(riderRepository, times(1))
                    .findByEmail("email@email.com");
            Mockito.verify(purchaseRepository, times(1))
                    .findTopByRiderAndStatusIsNot(any(), any());
            Mockito.verify(purchaseRepository, times(1))
                    .findTopByRiderIsNullOrderByDate();
            Mockito.verify(specificService, times(1)).setRiderName(any(),anyString());
            Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    public void testGetNewPurchaseForRider_whenSendInvalidData() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, store, "Miguel");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findTopByRiderIsNullOrderByDate())
                .thenReturn(Optional.of(p1));


        doThrow(InvalidValueException.class).when(specificService).setRiderName(any(), anyString());

        Assert.assertThrows( InvalidValueException.class, () -> {
            purchaseService.getNewPurchase("exampleToken");
        } );
        // test ...

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderIsNullOrderByDate();
        Mockito.verify(specificService, times(1)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }


    @Test
    public void testGetNewPurchaseForRiderValid() throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, store, "Miguel");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findTopByRiderIsNullOrderByDate())
                .thenReturn(Optional.of(p1));
        Mockito.when(purchaseRepository.save(p1)).thenReturn(p1);

        Purchase returned = purchaseService.getNewPurchase("exampleToken");
        assertThat(returned).isEqualTo(p1);
        assertThat(returned.getStatus()).isEqualTo(Status.ACCEPTED);
        assertThat(returned.getRider()).isEqualTo(r1);

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).setRiderName(anyString(),anyString());
        Mockito.verify(purchaseRepository, times(1)).save(any());
    }

    /* ----------------------------- *
     * Review Rider of Specific Order      *
     * ----------------------------- *
     */

    @Test
    void testWhenStoreRepositoryDoesntFindStoreByToken_ThenThrowInvalidLogin() {
        this.store.setToken("somerandomtoken_" + this.store.getToken());
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store.getToken(), this.purchase.getId(), 3);
        }, "Unauthorized store.");

        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    void testWhenPurchaseRepositoryDoesntFindPurchaseByOrderId_ThenThrowResourceNotFound() {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(-1L)).thenReturn(Optional.empty());

        String token = store.getToken();

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(token, -1L, 3);
        }, "Order not found.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    void whenPurchaseAlreadyHasAnAssociatedReviewValue_ThenThrowInvalidValue() {
        this.purchase.setRiderReview(3);
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        long purch_id = this.purchase.getId();
        String token = store.getToken();

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(token,purch_id , 4);
        }, "Invalid, purchased already had review.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    void whenPurchaseIsFromAnotherStore_thenThrowInvalidValue() {
        Address new_address = new Address();
        new_address.setAddress(RandomStringUtils.randomAlphabetic(17));
        new_address.setPostalCode(RandomStringUtils.randomAlphabetic(8));
        new_address.setCity(RandomStringUtils.randomAlphabetic(10));
        new_address.setCountry(RandomStringUtils.randomAlphabetic(10));

        Store new_store = new Store();
        new_store.setId(1L);
        new_store.setName(RandomStringUtils.randomAlphabetic(6));
        new_store.setDescription(RandomStringUtils.randomAlphabetic(10));
        new_store.setToken("eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjI1Njk5MzIxNzksImlhdCI6MTYyMzI0NzM3OX0.6uDsfOD8pSyqdVURX-LFpGoJmxmPcbJZwd79FLgUUO6yhoYWZIOROViGe3hI1AqHc2Qk08Us_fn7hohjIuelGQ");
        new_store.setAddress(new_address);

        Mockito.when(storeRepository.findByToken(new_store.getToken())).thenReturn(Optional.of(new_store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        long purch_id = this.purchase.getId();
        String token = new_store.getToken();

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(token, purch_id , 4);
        }, "Token passed belonged to a store where this purchase had not been made.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    void whenReviewNotDelivered_thenBadRequest() throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        long purch_id = this.purchase.getId();
        String token = store.getToken();


        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(token, purch_id, 4);
        }, "Invalid, purchase must be delivered first.");

        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    void whenEverythingIsOk_thenReturnPurchase() throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));

        this.purchase.setStatus(Status.DELIVERED);

        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        Purchase returned = purchaseService.reviewRiderFromSpecificOrder(this.store.getToken(), this.purchase.getId(), 4);

        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).saveAndFlush(any());

        assertThat(returned).isEqualTo(this.purchase);
        assertThat(returned.getRiderReview()).isEqualTo(4);
    }

    /* ----------------------------- *
     * GET LAST ORDER FOR RIDER      *
     * ----------------------------- *
     */

    @Test
    void testGetLastOrderForRiderWhenGetWithInvalidUser_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getLastOrderForRider(0, 10, "exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
    }

    @Test
    void testGetLastOrderForRiderWhenGetInvalidPageNo_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.getLastOrderForRider(-1, 10, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .findAllByRider(any(Rider.class), any(Pageable.class));
    }

    @Test
    void testGetLastOrderForRiderWhenGetInvalidPageSize_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        assertThrows(IllegalArgumentException.class, () -> {
            purchaseService.getLastOrderForRider(0, -1, "exampleToken");
        });

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0))
                .findAllByRider(any(Rider.class), any(Pageable.class));
    }

    @Test
    void testGetLastOrderForRiderGivenNoPurchases_whenGetPurchases_thenReturn0Records() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        Page<Purchase> pageRequest = new PageImpl(new ArrayList<>(), PageRequest.of(0, 10, Sort.by("date").descending()), new ArrayList<>().size());
        Mockito.when(purchaseRepository.findAllByRider(this.rider, PageRequest.of(0, 10, Sort.by("date").descending()))).thenReturn(pageRequest);

        Map<String, Object> found = purchaseService.getLastOrderForRider(0, 10, "exampleToken");
        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .findAllByRider(any(Rider.class), any(Pageable.class));

        assertThat(((List<Purchase>) found.get("orders"))).isEmpty();
        assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 0L).containsEntry("totalPages", 0);
    }

    @Test
    void testGetLastOrderForRiderGiven3Requests_whenGetRequests_thenReturn3Records() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, this.rider, store, "Miguel");
        Purchase p2 = new Purchase(addr, this.rider, store, "Mariana");
        Purchase p3 = new Purchase(addr, this.rider, store, "Carolina");

        List<Purchase> allPurchases = Arrays.asList(p1, p2, p3);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").descending()), allPurchases.size());

        Mockito.when(purchaseRepository.findAllByRider(this.rider, PageRequest.of(0, 10, Sort.by("date").descending()))).thenReturn(pageRequest);

        Map<String, Object> found = purchaseService.getLastOrderForRider(0, 10, "exampleToken");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1))
                .findAllByRider(any(Rider.class), any(Pageable.class));

        assertThat(((List<Purchase>) found.get("orders"))).hasSize(3).extracting(Purchase::getClientName).contains(p1.getClientName(), p2.getClientName(),
                p3.getClientName());

        assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 3L)
                .containsEntry("totalPages", 1);
    }


    /* ----------------------------- *
     * CLIENT MAKES NEW ORDER TESTS  *
     * ----------------------------- *
     */

    @Test
    void testPostNewOrder_whenInvalidStoreToken_thenThrow() {
        Mockito.when(jwtUserDetailsService.getStoreFromToken("invalid-token")).thenReturn(null);

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.receiveNewOrder("invalid-token", any());
        }, "There is no Store associated with this token");
        Mockito.verify(jwtUserDetailsService, times(1))
                .getStoreFromToken("invalid-token");
    }

    @Test
    public void testPostNewOrder_whenOneFieldIsMissing_thenThrow() throws InvalidValueException, InvalidLoginException {
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");

        Mockito.when(jwtUserDetailsService.getStoreFromToken("token")).thenReturn(store);

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("date", 33333);

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.receiveNewOrder("token", input);
        }, "invalid data");
        Mockito.verify(jwtUserDetailsService, times(1))
                .getStoreFromToken("token");

    }

    @Test
    public void testPostNewOrder_whenOneFieldIsBad_thenThrow() throws InvalidValueException, InvalidLoginException {
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");

        Mockito.when(jwtUserDetailsService.getStoreFromToken("token")).thenReturn(store);

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("date", 33333L);
        input.put("address", null);

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.receiveNewOrder("token", input);
        }, "invalid data");
        Mockito.verify(jwtUserDetailsService, times(1))
                .getStoreFromToken("token");
    }

    @Test
    public void testPostNewOrder_whenEverythingGood_thenReturnPurchase() throws InvalidValueException, InvalidLoginException {
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");

        Mockito.when(jwtUserDetailsService.getStoreFromToken("token")).thenReturn(store);
        Mockito.when(addressRepository.save(address)).thenReturn(address);

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("address", address.getMap());

        Purchase purchase = purchaseService.receiveNewOrder("token", input);

        assertThat(purchase.getAddress().getAddress()).isEqualTo(address.getAddress());
        assertThat(purchase.getClientName()).isEqualTo("mmm");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getStoreFromToken("token");
        Mockito.verify(addressRepository, times(1))
                .save(address);
    }




    /* ------------------------------- *
     * GET NEW PURCHASE WITH LOC TESTS *
     * ------------------------------- *
     */

    @Test
    void testGetNewPurchaseWithLocForRider_whenInvalidUser() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 1.0, 2.0);
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(0))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(0))
                .findAllByRiderIsNullOrderByDate(any());
        Mockito.verify(specificService, times(0)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    void testGetNewPurchaseForRiderLoc_whenRiderHasAnOrderAlreadyThrows() {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");
        Purchase p1 = new Purchase(addr, r1, store, "Miguel");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));

        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.of(p1));

        assertThrows(ForbiddenRequestException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 1.0, 2.0);
        }, "This rider still has an order to deliver");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(0))
                .findAllByRiderIsNullOrderByDate(any());
        Mockito.verify(specificService, times(0)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    void testGetNewPurchaseForRiderLoc_whenThereIsNoMorePurchases() {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());

        Page<Purchase> pageRequest = new PageImpl(new ArrayList<>(), PageRequest.of(0, 15), new ArrayList<>().size());

        Mockito
                .when(purchaseRepository.findAllByRiderIsNullOrderByDate(PageRequest.of(0, 15, Sort.by("date").ascending())))
                .thenReturn(pageRequest);

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 1.0, 2.0);
        }, "There are no more orders available");

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(1))
                .findAllByRiderIsNullOrderByDate(any());
        Mockito.verify(specificService, times(0)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    void testGetNewPurchaseWithLocForRider_whithInvalidLoc_thenThrow() throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException, InvalidValueException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr_store_far = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_far = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_far,"http://localhost:8081/delivery/",  5.0, 5.0);

        Address addr_far = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_far = new Purchase(addr_far, store_far, "Miguel");

        // the far one was created first; but the second one is the one that should be returned
        List<Purchase> allPurchases = Arrays.asList(p1_far);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").ascending()), 1);


        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));


        assertThrows(InvalidValueException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 100.0, 0.0);
        }, "Invalid values for coordinates");


        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(specificService, times(0)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }


    @Test
    void testGetNewPurchaseWithLocForRiderValid() throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException, InvalidValueException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr_store_far = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_far = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_far,"http://localhost:8081/delivery/", 5.0, 5.0);

        Address addr_store_close = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_close = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_close,"http://localhost:8081/delivery/", 1.0, 1.0);

        Address addr_far = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_far = new Purchase(addr_far, store_far, "Miguel");

        Address addr_close = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_close = new Purchase(addr_close, store_close, "Miguel");

        // the far one was created first; but the second one is the one that should be returned
        List<Purchase> allPurchases = Arrays.asList(p1_far, p1_close);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").ascending()), 2);


        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findAllByRiderIsNullOrderByDate(any()))
                .thenReturn(pageRequest);

        Mockito.when(purchaseRepository.save(p1_close)).thenReturn(p1_close);

        Purchase returned = purchaseService.getNewPurchaseLoc("exampleToken", 0.0, 0.0);
        assertThat(returned).isEqualTo(p1_close);
        assertThat(returned.getStatus()).isEqualTo(Status.ACCEPTED);
        assertThat(returned.getRider()).isEqualTo(r1);

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(specificService, times(1)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(1)).save(any());
    }

    @Test
    public void testGetNewOrderLocForRider_whenErrorConnection() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr_store_far = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_far = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_far,"http://localhost:8081/delivery/", 5.0, 5.0);

        Address addr_store_close = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_close = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_close,"http://localhost:8081/delivery/", 1.0, 1.0);

        Address addr_far = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_far = new Purchase(addr_far, store_far, "Miguel");

        Address addr_close = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_close = new Purchase(addr_close, store_close, "Miguel");

        // the far one was created first; but the second one is the one that should be returned
        List<Purchase> allPurchases = Arrays.asList(p1_far, p1_close);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").ascending()), 2);


        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findAllByRiderIsNullOrderByDate(any()))
                .thenReturn(pageRequest);
        doThrow(UnreachableServiceException.class).when(specificService).setRiderName(any(), anyString());

        Assert.assertThrows( UnreachableServiceException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 0.0, 0.0);
        } );
        // test ...

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(1))
                .findAllByRiderIsNullOrderByDate(any());
        Mockito.verify(specificService, times(1)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    @Test
    public void testGetNewOrdeLocForRider_whenSendInvalidData() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr_store_far = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_far = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_far,"http://localhost:8081/delivery/", 5.0, 5.0);

        Address addr_store_close = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_close = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_close,"http://localhost:8081/delivery/", 1.0, 1.0);

        Address addr_far = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_far = new Purchase(addr_far, store_far, "Miguel");

        Address addr_close = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_close = new Purchase(addr_close, store_close, "Miguel");

        // the far one was created first; but the second one is the one that should be returned
        List<Purchase> allPurchases = Arrays.asList(p1_far, p1_close);
        Page<Purchase> pageRequest = new PageImpl(allPurchases, PageRequest.of(0, 10, Sort.by("date").ascending()), 2);


        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(r1));
        Mockito
                .when(purchaseRepository.findTopByRiderAndStatusIsNot(r1, Status.DELIVERED))
                .thenReturn(Optional.empty());
        Mockito
                .when(purchaseRepository.findAllByRiderIsNullOrderByDate(any()))
                .thenReturn(pageRequest);


        doThrow(InvalidValueException.class).when(specificService).setRiderName(any(), anyString());

        Assert.assertThrows( InvalidValueException.class, () -> {
            purchaseService.getNewPurchaseLoc("exampleToken", 0.0, 0.0);
        } );
        // test ...

        Mockito.verify(jwtUserDetailsService, times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, times(1))
                .findByEmail("email@email.com");
        Mockito.verify(purchaseRepository, times(1))
                .findTopByRiderAndStatusIsNot(any(), any());
        Mockito.verify(purchaseRepository, times(1))
                .findAllByRiderIsNullOrderByDate(any());
        Mockito.verify(specificService, times(1)).setRiderName(any(),anyString());
        Mockito.verify(purchaseRepository, times(0)).save(any());
    }

    /* ----------------------------- *
     * GET TOP DELIVERED CITIES      *
     * ----------------------------- *
     */

    @Test
    void testGetTopDeliveredCities_whenEverythingIsOK_thenReturn() {
        List<Object[]> repositoryResponse = new ArrayList<>();
        repositoryResponse.add(new Object[]{"Lisboa", 5});
        repositoryResponse.add(new Object[]{"Faro", 9});
        repositoryResponse.add(new Object[]{"Mirandela", 11});
        repositoryResponse.add(new Object[]{"Figueira da Foz", 1});
        repositoryResponse.add(new Object[]{"Minho", 2});

        Mockito.when(purchaseRepository.getTopFiveCitiesOfPurchases()).thenReturn(repositoryResponse);

        Map<String, Object> response = purchaseService.getTop5Cities();

        assertThat(response.get("Lisboa")).isEqualTo(5);
        assertThat(response.get("Faro")).isEqualTo(9);
        assertThat(response.get("Mirandela")).isEqualTo(11);
        assertThat(response.get("Figueira da Foz")).isEqualTo(1);
        assertThat(response.get("Minho")).isEqualTo(2);
        assertThat(response.size()).isEqualTo(5);


        Mockito.verify(purchaseRepository, times(1)).getTopFiveCitiesOfPurchases();
    }

    @Test
    void testGetTopDeliveredCities_whenThereAreNot5DifferentCities_thenReturnMap() {
        List<Object[]> repositoryResponse = new ArrayList<>();
        repositoryResponse.add(new Object[]{"Guarda", 8});
        repositoryResponse.add(new Object[]{"Castelo Branco", 3});

        Mockito.when(purchaseRepository.getTopFiveCitiesOfPurchases()).thenReturn(repositoryResponse);

        Map<String, Object> response = purchaseService.getTop5Cities();

        assertThat(response.get("Guarda")).isEqualTo(8);
        assertThat(response.get("Castelo Branco")).isEqualTo(3);
        assertThat(response.size()).isEqualTo(2);

        Mockito.verify(purchaseRepository, times(1)).getTopFiveCitiesOfPurchases();
    }

    @Test
    void testGetTopDeliveredCities_whenNoPurchases_thenReturnMap() {
        List<Object[]> repositoryResponse = new ArrayList<>();

        Mockito.when(purchaseRepository.getTopFiveCitiesOfPurchases()).thenReturn(repositoryResponse);

        Map<String, Object> response = purchaseService.getTop5Cities();

        assertThat(response).isEmpty();

        Mockito.verify(purchaseRepository, times(1)).getTopFiveCitiesOfPurchases();
    }

}

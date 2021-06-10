package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;


import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.data.domain.*;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {
    private Rider rider = new Rider("example", "pwd", "email@email.com");


    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private RiderRepository riderRepository;

    /* ----------------------------- *
     * UPDATE PURCHASE STATUS        *
     * ----------------------------- *
     */
    @Test
    public void testUpdatePurchaseStatusOfRider_whenInvalidUser() {
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
    public void testUpdatePurchaseStatusOfRider_whenRiderHasNoPurchaseThrows() {
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
    }

    @Test
    public void testUpdateCurrentPurchaseOfRiderValid() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
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
        Purchase current = purchaseService.updatePurchaseStatus("tokenExample");
        assertThat(current).isEqualTo(p1);
        assertThat(current.getStatus()).isEqualTo(Status.PICKED_UP);
        assertThat(current.getRider()).isEqualTo(r1);
        Mockito.verify(jwtUserDetailsService, times(1)).getEmailFromToken(any());
        Mockito.verify(riderRepository, times(1)).findByEmail(any());
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderAndStatusIsNot(any(), any());
    }


    /* ----------------------------- *
     * GET CURRENT PURCHASE TESTS    *
     * ----------------------------- *
     */

    @Test
    public void testGetCurrentPurchaseOfRider_whenInvalidUser() {
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
    public void testGetCurrentPurchaseOfRider_whenRiderHasNoOrderThrows() {
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
    public void testGetCurrentPurchaseOfRiderValid() throws InvalidLoginException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
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
    public void testGetNewPurchaseForRider_whenInvalidUser() {
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
    public void testGetNewPurchaseForRider_whenRiderHasAnOrderAlreadyThrows() {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");
        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
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
    }

    @Test
    public void testGetNewPurchaseForRider_whenThereIsNoMorePurchases() {
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
    }

    @Test
    public void testGetNewPurchaseForRiderValid() throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        // set up ...
        Rider r1 = new Rider("example", "pwd", "email@email.com");

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
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
    }


    /* ----------------------------- *
     * GET LAST ORDER FOR RIDER      *
     * ----------------------------- *
     */

    @Test
    public void testGetLastOrderForRiderWhenGetWithInvalidUser_thenThrow() {
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
    public void testGetLastOrderForRiderWhenGetInvalidPageNo_thenThrow() {
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
    public void testGetLastOrderForRiderWhenGetInvalidPageSize_thenThrow() {
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
    public void testGetLastOrderForRiderGivenNoPurchases_whenGetPurchases_thenReturn0Records() throws InvalidLoginException {
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
        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(0L);
        assertThat(found.get("totalPages")).isEqualTo(0);
    }

    @Test
    public void testGetLastOrderForRiderGiven3Requests_whenGetRequests_thenReturn3Records() throws InvalidLoginException {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.of(this.rider));

        Address addr = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);
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

        assertThat(found.get("currentPage")).isEqualTo(0);
        assertThat(found.get("totalItems")).isEqualTo(3L);
        assertThat(found.get("totalPages")).isEqualTo(1);
    }


}

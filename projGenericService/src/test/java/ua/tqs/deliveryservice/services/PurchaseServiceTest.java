package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


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

    /*
    public Purchase getCurrentPurchase(String token) throws InvalidLoginException, ResourceNotFoundException {
        String email = jwtUserDetailsService.getEmailFromToken(token);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));
        Purchase unfinished = purchaseRepository.findTopByRiderAndStatusIsNot(rider, Status.DELIVERED).orElseThrow( ()-> new ResourceNotFoundException("This rider hasn't accepted an order yet"));
        return unfinished;
    }
    falta : qdo n tem nenhuma encomenda!
    */

    @Test
    public void testGetCurrentPurchaseOfRiderValid_whenInvalidUser() {
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

    /* // TODO: finish
    @Test
    public void testGetCurrentPurchaseOfRiderValid_whenRiderHasNoOrder() {
        Rider r1 = new Rider("example", "pwd", "email@email.com");

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
     */

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

    // --- delete down ---
    @Test
    public void givenAvailableOrder_whenGetAvailableOrderForRider_thenGetThatOrder() {
        // setting up ...
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);
        Purchase available = new Purchase(addr1, store1, "João");

        Mockito
                .when( purchaseRepository.findTopByRiderIsNullOrderByDate() )
                .thenReturn(java.util.Optional.of(available));

        // test ...
        Purchase ret = purchaseService.getAvailableOrderForRider();
        assertThat(ret).isEqualTo(available);
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderIsNullOrderByDate();
    }

    @Test
    public void givenNoAvailableOrder_whenGetAvailableOrderForRider_thenGetNull() {
        // setting up ...
        Mockito
                .when( purchaseRepository.findTopByRiderIsNullOrderByDate() )
                .thenReturn( null );

        // test ...
        Purchase ret = purchaseService.getAvailableOrderForRider();
        assertThat(ret).isNull();
        Mockito.verify(purchaseRepository, times(1)).findTopByRiderIsNullOrderByDate();
    }

    @Test
    public void givenRiderAndPurchase_whenAcceptOrder_thenPurchasedIsChanged() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);
        Purchase available = new Purchase(addr1, store1, "João");

        Mockito
                .when(purchaseRepository.save(available))
                .thenReturn(available);

        // test ...
        Purchase expectedRet = new Purchase(addr1, r1, store1, "João");
        expectedRet.setStatus(Status.ACCEPTED);

        Purchase ret = purchaseService.acceptOrder(r1, available);
        assertThat(ret).isEqualTo(expectedRet);
    }


    @Test
    public void givenRiderWithoutOrder_whenGetCurrentRiderOrder_returnNull() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");

        Mockito
                .when( purchaseRepository.findTopByRiderAndStatusIsNot( r1, Status.DELIVERED) )
                .thenReturn(null);

        // test ...

        Purchase ret = purchaseService.getCurrentRiderOrder(r1);
        assertThat(ret).isNull();
        Mockito.verify( purchaseRepository, times(1) ).findTopByRiderAndStatusIsNot(r1, Status.DELIVERED);
    }

    @Test
    public void givenRiderWithOrder_whenGetCurrentRiderOrder_returnOrder() {
        // setting up ...
        Rider r1 = new Rider("MM", "pwd", "r1@email.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua Loja Loja, n. 23", "3212-333", "Porto", "Portugal");
        Store store1 = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr2);

        Purchase purch = new Purchase(addr1, r1, store1, "João");
        purch.setStatus(Status.ACCEPTED);

        Mockito
                .when( purchaseRepository.findTopByRiderAndStatusIsNot( r1, Status.DELIVERED) )
                .thenReturn(java.util.Optional.of(purch));

        // test ...
        Purchase ret = purchaseService.getCurrentRiderOrder(r1);
        assertThat(ret).isNotNull();
        assertThat(ret).isEqualTo(purch);
        Mockito.verify( purchaseRepository, times(1) ).findTopByRiderAndStatusIsNot(r1, Status.DELIVERED);
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

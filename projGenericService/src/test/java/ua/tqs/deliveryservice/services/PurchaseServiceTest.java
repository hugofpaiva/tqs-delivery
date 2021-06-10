package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;

import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    private Rider rider = new Rider("example", "pwd", "email@email.com");
    private Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
    private Store store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", this.address);
    private Purchase purchase = new Purchase(this.address, this.rider, this.store, "Joana");

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;



    @Test
    public void testWhenStoreRepositoryDoesntFindStoreByToken_ThenThrowInvalidLogin() {
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
    public void testWhenPurchaseRepositoryDoesntFindPurchaseByOrderId_ThenThrowResourceNotFound() {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store.getToken(), -1L, 3);
        }, "Order not found.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void whenPurchaseAlreadyHasAnAssociatedReviewValue_ThenThrowInvalidValue() {
        this.purchase.setRiderReview(3);
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store.getToken(), this.purchase.getId(), 4);
        }, "Invalid, purchased already had review.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void whenPurchaseIsFromAnotherStore_thenThrowInvalidValue() {
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

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(new_store.getToken(), this.purchase.getId(), 4);
        }, "Token passed belonged to a store where this purchase had not been made.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void whenEverythingIsOk_thenReturnPurchase() throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
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


    @Test
    public void testGetLastOrderForRiderWhenGetWithInvalidUser_thenThrow() {
        Mockito.when(jwtUserDetailsService.getEmailFromToken("exampleToken")).thenReturn("email@email.com");
        Mockito.when(riderRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.getLastOrderForRider(0, 10, "exampleToken");
        }, "There is no Rider associated with this token");

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1))
                .getEmailFromToken("exampleToken");
        Mockito.verify(riderRepository, VerificationModeFactory.times(1))
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

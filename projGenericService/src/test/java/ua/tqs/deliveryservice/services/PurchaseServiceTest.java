package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private PurchaseService purchaseService;


    private String rider_name;
    private String rider_pwd;
    private String rider_email;

    private String address_address;
    private String address_postalCode;
    private String address_city;
    private String address_country;

    private String store_name;
    private String store_descricao;
    private String store_token;

    private String purchase_clientName;

    private Rider rider;
    private Address address;
    private Store store;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        this.rider_name = RandomStringUtils.randomAlphabetic(7);
        this.rider_pwd = RandomStringUtils.randomAlphabetic(10);
        this.rider_email = RandomStringUtils.randomAlphabetic(20);

        this.address_address = RandomStringUtils.randomAlphabetic(17);
        this.address_postalCode = RandomStringUtils.randomAlphabetic(8);
        this.address_city = RandomStringUtils.randomAlphabetic(10);
        this.address_country = RandomStringUtils.randomAlphabetic(10);

        this.store_name = RandomStringUtils.randomAlphabetic(6);
        this.store_descricao = RandomStringUtils.randomAlphabetic(10);
        this.store_token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";

        this.purchase_clientName = RandomStringUtils.randomAlphabetic(10);

        this.rider = new Rider(rider_name, rider_pwd, rider_email);
        this.address = new Address(address_address, address_postalCode, address_city, address_country);
        this.store = new Store(store_name, store_descricao, store_token, this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, purchase_clientName);
    }


    @Test
    public void testWhenStoreRepositoryDoesntFindStoreByToken_ThenThrowInvalidLogin() {
        this.store.setToken("somerandomtoken_" + this.store_token);
        Mockito.when(storeRepository.findByToken(this.store_token)).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store_token, this.purchase.getId(), 3);
        }, "Unauthorized store.");

        Mockito.verify(storeRepository, VerificationModeFactory.times(1)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void testWhenPurchaseRepositoryDoesntFindPurchaseByOrderId_ThenThrowResourceNotFound() {
        Mockito.when(storeRepository.findByToken(this.store_token)).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store_token, -1L, 3);
        }, "Order not found.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(2)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void whenPurchaseAlreadyHasAnAssociatedReviewValue_ThenThrowInvalidValue() {
        this.purchase.setRiderReview(3);
        Mockito.when(storeRepository.findByToken(this.store_token)).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        assertThrows(InvalidValueException.class, () -> {
            purchaseService.reviewRiderFromSpecificOrder(this.store_token, this.purchase.getId(), 4);
        }, "Invalid, purchased already had review.");


        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(2)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(2)).findById(anyLong());
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
        Mockito.verify(storeRepository, VerificationModeFactory.times(2)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(2)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(0)).saveAndFlush(any());
    }

    @Test
    public void whenEverythingIsOk_thenReturnPurchase() throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {
        Mockito.when(storeRepository.findByToken(this.store.getToken())).thenReturn(Optional.of(this.store));
        Mockito.when(purchaseRepository.findById(this.purchase.getId())).thenReturn(Optional.of(this.purchase));

        Purchase returned = purchaseService.reviewRiderFromSpecificOrder(this.store_token, this.purchase.getId(), 4);

        // two times findByToken is called because the best usage of the Optional class is to first
        // check it the object is empty or present, and then get it with .get(), thus twice
        Mockito.verify(storeRepository, VerificationModeFactory.times(2)).findByToken(anyString());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(2)).findById(anyLong());
        Mockito.verify(purchaseRepository, VerificationModeFactory.times(1)).saveAndFlush(any());

        assertThat(returned, equalTo(this.purchase));
        assertThat(returned.getRiderReview(), equalTo(4));
    }

}

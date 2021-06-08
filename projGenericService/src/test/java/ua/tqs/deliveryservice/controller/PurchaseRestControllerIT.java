package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PurchaseRestControllerIT {
    private Rider rider;
    private Address address;
    private Store store;
    private Purchase purchase;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @LocalServerPort
    int randomServerPort;

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
            .withUsername("demo")
            .withPassword("demopw")
            .withDatabaseName("delivery");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    @AfterEach
    public void destroyAll() {
        purchaseRepository.deleteById(this.purchase.getId());
        purchaseRepository.flush();

        storeRepository.deleteById(this.store.getId());
        storeRepository.flush();

        addressRepository.deleteById(this.address.getId());
        addressRepository.flush();

        personRepository.deleteById(this.rider.getId());
        personRepository.flush();


        this.rider = new Rider();
        this.address = new Address();
        this.store = new Store();
        this.purchase = new Purchase();
    }

    @BeforeEach
    public void beforeEachSetUp() {
        this.rider = new Rider("Joao", bcryptEncoder.encode("aRightPassword"), "TQS_delivery@example.com");
        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");

        personRepository.saveAndFlush(this.rider);
        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
        purchaseRepository.saveAndFlush(this.purchase);
    }


    // ----------------------------------------------
    // --               review tests               --
    // ----------------------------------------------
    @Test
    public void testReviewWhenInvalidMin_thenBadRequest() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", -1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidMax_thenBadRequest() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", 6L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testReviewWhenInvalidOrderId_thenNotFound() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/5/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenNoReviewSent_thenNotFound() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenReviewAlreadyExisted_thenNotFound() {
        this.purchase.setRiderReview(3);

        purchaseRepository.saveAndFlush(this.purchase);

        Map<String, Long> data = new HashMap<>();
        data.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + this.purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenWrongToken_thenUnauthorized() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Beareaaar " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + this.purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testValidTokenButWrongStoreAndPurchase_thenCodeBadRequest() {
        Address new_address = new Address("Uma Rua Nova, n77", "5656-221", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(new_address);

        Store new_store = new Store("OutraLoja", "Descricao", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjIzMTczNjU3MzUsImlhdCI6MTYyMzE0MTczNX0._gvC50t5mx5rwoCrXCWhFRiM_RZzCrsRNeLXRVi1IUurV6mruKtehBGIYFYTrQ5dkKIqcGk5yLFTxYQwG8q8dA", new_address);
        Purchase new_purchase = new Purchase(new_address, this.rider, new_store, "Outro");
        storeRepository.saveAndFlush(new_store);
        purchaseRepository.saveAndFlush(new_purchase);

        Map<String, Long> data = new HashMap<>();
        data.put("review", 4L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + new_store.getToken());
        HttpEntity<Map<String, Long> > entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + this.purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));


        purchaseRepository.deleteById(new_purchase.getId());
        purchaseRepository.flush();

        storeRepository.deleteById(new_store.getId());
        storeRepository.flush();

        addressRepository.deleteById(new_address.getId());
        addressRepository.flush();
    }

    @Test
    public void testValidOrderIdValidReviewValue_thenCodeOK() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange( getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/store"; }

}

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderRestControllerIT {
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
        String patch = "{\"review\":\"-1\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        System.out.println(this.store.getToken());

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        System.out.println(response);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidMax_thenBadRequest() {
        String patch = "{\"review\":\"6\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidReviewValue_thenNotFound() {
        String patch = "{\"review\":\"a\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidOrderId_thenNotFound() {
        String patch = "{\"review\":\"3\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/5/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenNoReviewSent_thenNotFound() {
        String patch = "{\"review\":\"\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenReviewAlreadyExisted_thenNotFound() {
        String patch = "{\"review\":\"3\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testValidOrderIdValidReviewValue_thenCodeOK() {
        this.purchase.setRiderReview(3);
        purchaseRepository.saveAndFlush(this.purchase);

        String patch = "{\"review\":\"4\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<String> entity = new HttpEntity<>(patch, headers);

        ResponseEntity<String> response = testRestTemplate.exchange( getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/store"; }

}

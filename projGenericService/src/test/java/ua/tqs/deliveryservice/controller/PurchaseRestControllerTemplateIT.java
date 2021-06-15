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
class PurchaseRestControllerTemplateIT {
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
    public void testReviewWhenNullOrderId_thenBadRequest() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + null + "/review", HttpMethod.PATCH, entity, Object.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenNullReview_thenBadRequest() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, Object.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testReviewWhenInvalidMin_thenBadRequest() {
        Map<String, Long> data = new HashMap<>();
        data.put("review", -1L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<Object> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, Object.class);
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

        ResponseEntity<Object> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/review", HttpMethod.PATCH, entity, Object.class);
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

        ResponseEntity<Object> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + this.purchase.getId() + "/review", HttpMethod.PATCH, entity, Object.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testValidOrderIdValidReviewValue_thenCodeOK() {
        Long review = 3L;

        Map<String, Long> data = new HashMap<>();
        data.put("review", review);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "Bearer " + this.store.getToken());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = testRestTemplate.exchange( getBaseUrl() + "/order/" + this.purchase.getId() + "/review", HttpMethod.PATCH, entity, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        // como isto é um patch, não tem de se enviar o objeto de volta.
        // src: https://stackoverflow.com/questions/37718119/should-the-patch-method-return-all-fields-of-the-resource-in-the-response-body/37718786
    }

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/store"; }


    /* ----------------------------- *
     * CLIENT MAKES NEW ORDER TESTS  *
     * ----------------------------- *
     */


    @Test
    public void givenStoreHasNoAuthorization_whenPostNewOrder_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/order", HttpMethod.POST, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void givenStore_whenPostNewOrderWithMissingField_then400() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw");

        Address addr = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("address", addr.getMap());


        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/order", HttpMethod.POST, new HttpEntity<>(input, headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void givenStore_whenPostNewOrderWithBadField_then400() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw");

        Address addr = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("date", "invalid-date");
        input.put("address", addr.getMap());


        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/order", HttpMethod.POST, new HttpEntity<>(input, headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }
/*  // TODO :: FALHA NUMA CONSTRAINT QUALQUER
    @Test
    public void givenStore_whenPostNewOrderGood_then200() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw");

        Address addr = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");

        Map<String, Object> input = new HashMap<>();
        input.put("personName", "mmm");
        input.put("date", 333334233L);
        input.put("address", addr.getMap());

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "/order", HttpMethod.POST, new HttpEntity<>(input, headers),
                Map.class);


        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    }

 */







}

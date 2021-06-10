package ua.tqs.deliveryservice.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RiderRestControllerTemplateIT {
    private Rider rider;
    private Address address;
    private Store store;
    private Purchase purchase;
    private String token;

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

    @LocalServerPort
    int randomServerPort;

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

    @BeforeEach
    public void beforeEachSetUp() {
        this.rider = new Rider("TQS_delivery@example.com", bcryptEncoder.encode("aRightPassword"), "Joao");
        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");

        personRepository.saveAndFlush(this.rider);

        JwtRequest request = new JwtRequest(this.rider.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
        purchaseRepository.saveAndFlush(this.purchase);
    }

    @AfterEach
    public void destroyAll() {
        this.deleteAll();
    }

    // ----------------------------------------------
    // --              Orders History tests        --
    // ----------------------------------------------


    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "orders?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "orders?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRiderOrderHistory_thenStatus200() {
        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "orders", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Purchase> orders = mapper.convertValue(
                found.get("orders"),
                new TypeReference<List<Purchase>>() {
                }
        );

        Assertions.assertThat(orders).hasSize(1).extracting(Purchase::getDate).contains(this.purchase.getDate());

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);


    }

    @Test
    public void testGetRiderOrderHistoryPageNoWithoutResults_thenNoResults() {
        purchaseRepository.delete(this.purchase);
        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "orders", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Purchase> orders = mapper.convertValue(
                found.get("orders"),
                new TypeReference<List<Purchase>>() {
                }
        );

        Assertions.assertThat(orders).hasSize(0);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(0);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(0);
    }

    @Test
    public void testGetRiderOrderHistoryPageNoAndLimitedPageSize_thenLimitedResults() {
        Address addr1 = new Address("Rua ABC, n. 89", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 79", "4444-555", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(addr1);
        addressRepository.saveAndFlush(addr2);
        Purchase p2 = new Purchase(addr1, this.rider, this.store, "Manel");
        Purchase p3 = new Purchase(addr2, this.rider, this.store, "Manuela");
        purchaseRepository.saveAndFlush(p2);
        purchaseRepository.saveAndFlush(p3);

        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "orders?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Purchase> orders = mapper.convertValue(
                found.get("orders"),
                new TypeReference<List<Purchase>>() {
                }
        );

        Assertions.assertThat(orders).hasSize(2);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(3);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(2);
    }

    @Test
    public void testGetRiderOrderHistoryButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "orders?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    // ----------------------------------------------
    // --               status tests               --
    // ----------------------------------------------

    @Disabled
    public void testOrderStatusWhenStringId_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/a/status", HttpMethod.PATCH, entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Disabled
    public void testOrderStatusWhenInvalidId_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/1/status", HttpMethod.PATCH, entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Disabled
    public void testOrderStatusWhenInvalidStatus_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/4/status", HttpMethod.PATCH, entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }


    @Disabled
    public void testOrderStatusEverythingValid_thenOK() {
        HashMap<String, Object> expected = new HashMap<>();
        expected.put("order_id", purchase.getId());
        expected.put("status", Status.getNext(purchase.getStatus()));

        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/status", HttpMethod.PATCH, entity, Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().toString(), equalTo(expected.toString()));
    }

    // ----------------------------------------------
    // --              register tests              --
    // ----------------------------------------------

    @Test
    public void testRegisterBadPwd_thenBadRequest() {
        Map<String, String> data = new HashMap<>();

        data.put("name", "delivery tqs tests");
        data.put("email", "delivery@tqs.com");
        data.put("pwd", "123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(data, headers);

        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testRegisterBadEmail_thenBadRequest() {
        Map<String, String> data = new HashMap<>();

        data.put("name", "delivery tqs tests");
        data.put("email", "this is not_an!email");
        data.put("pwd", "123but_the_password_is_ok");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(data, headers);

        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testRegisterBadEmailAndPwd_thenBadRequest() {
        Map<String, String> data = new HashMap<>();

        data.put("name", "delivery tqs tests");
        data.put("email", "this is not_an!email");
        data.put("pwd", "badpwd");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(data, headers);

        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Disabled
    public void testRegisterEverythingValid_thenCreated() {
        Map<String, String> data = new HashMap<>();

        data.put("name", "delivery tqs tests");
        data.put("email", "delivery@tqs.com");
        data.put("pwd", "delivery__password__strong_!enough");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(data, headers);

        ResponseEntity<HttpStatus> response = testRestTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity, HttpStatus.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
    }


    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/rider/";

    }

    public void deleteAll() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        storeRepository.deleteAll();
        storeRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();

    }

}
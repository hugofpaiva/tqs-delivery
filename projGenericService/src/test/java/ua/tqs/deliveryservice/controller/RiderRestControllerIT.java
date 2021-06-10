package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.*;
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
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RiderRestControllerIT {
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

    private String token;

    @LocalServerPort
    int randomServerPort;

    @Container
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
                .withUsername("demo")
                .withPassword("demopw")
                .withDatabaseName("shop");


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

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "/order/" + purchase.getId() + "/status", HttpMethod.PATCH, entity,  Map.class);

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

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/rider/"; }
}

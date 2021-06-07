package ua.tqs.deliveryservice.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
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
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RiderRestControllerIT {
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


    @BeforeEach
    public void beforeEachSetUp() {
        Rider rider = new Rider();
        System.out.println(rider.getId());
        if ( personRepository.findById(rider.getId()).isEmpty() ) {
            System.out.println("Entrei 2x");
            System.out.println(rider.getEmail());
            rider.setEmail("TQS_delivery@example.com");
            System.out.println(rider.getEmail());
            rider.setPwd(bcryptEncoder.encode("aRightPassword"));
            rider.setName("Joao");
            personRepository.saveAndFlush(rider);
        }

        Address address = new Address();

        if (addressRepository.findById(address.getId()).isEmpty()) {
            address.setAddress("Universidade de Aveiro");
            address.setAddress("3800-000");
            address.setCountry("Aveiro");
            address.setCountry("Portugal");
            addressRepository.saveAndFlush(address);
        }

        Store store = new Store();

        if (storeRepository.findById(store.getId()).isEmpty()) {
            store.setName("HumberPecas");
            store.setDescription("Peça(s) rápido");
            store.setAddress(address);
            store.setToken("somestringnewtoken");
            storeRepository.saveAndFlush(store);
        }

        Purchase purchase = new Purchase();

        if (purchaseRepository.findById(purchase.getId()).isEmpty()) {
            purchase.setAddress(address);
            purchase.setStatus(Status.ACCEPTED);
            purchase.setRider(rider);
            purchase.setStore(store);
            purchase.setClientName("TQS Project");
            purchaseRepository.saveAndFlush(purchase);
        }

        JwtRequest request = new JwtRequest(rider.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);

        this.token = response.getBody().get("token").toString();
    }

    // ----------------------------------------------
    // --               status tests               --
    // ----------------------------------------------
    @Test
    public void testOrderStatusWhenStringId_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/a/status", HttpMethod.PATCH, entity, Map.class);
        System.out.println(response);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusWhenInvalidId_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/1/status", HttpMethod.PATCH, entity, Map.class);
        System.out.println(response);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusWhenInvalidStatus_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/4/status", HttpMethod.PATCH, entity, Map.class);
        System.out.println(response);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusEverythingValid_thenOK() {
        Map<String, Object> data = new HashMap<>();
        data.put("order_id", 0);
        data.put("status", Status.ACCEPTED.toString());
        JSONObject json = new JSONObject(data);

        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "/order/0/status", HttpMethod.PATCH, entity,  Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(json.toString()));
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

    @Test
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

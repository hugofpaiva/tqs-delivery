package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RiderRestControllerIT {
    private Rider rider;
    private Rider newRider;
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
    private String newRiderToken;

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

        personRepository.deleteById(this.newRider.getId());
        personRepository.flush();

        this.rider = new Rider();
        this.address = new Address();
        this.store = new Store();
        this.purchase = new Purchase();
        this.newRider = new Rider();
    }

    @BeforeEach
    public void beforeEachSetUp() {
        this.rider = new Rider("TQS_delivery@example.com", bcryptEncoder.encode("aRightPassword"), "Joao");
        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");

        this.newRider = new Rider("João", bcryptEncoder.encode("aRightPassword"), "new_TQS_delivery@example.com");
        personRepository.saveAndFlush(this.rider);
        personRepository.saveAndFlush(this.newRider);

        JwtRequest request = new JwtRequest(this.rider.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        JwtRequest newRequest = new JwtRequest(newRider.getEmail(), "aRightPassword");
        ResponseEntity<Map> newResponse = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", newRequest, Map.class);
        this.newRiderToken = newResponse.getBody().get("token").toString();

        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
        purchaseRepository.saveAndFlush(this.purchase);
    }

    @Test // DONE
    public void whenRiderHasCurrentOrder_testGetCurrentOrder() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        HttpEntity<Map> entity = new HttpEntity<>(empty, headers);

        Map<String, Object> expectedResp = this.purchase.getMap();

        ParameterizedTypeReference<Map<String, Map<String, Object>>> responseType = new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {};

        ResponseEntity<Map<String, Map<String, Object>>> response = testRestTemplate.exchange(getBaseUrl() + "order/current", HttpMethod.GET, entity, responseType);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        Map<String, Map<String, Object>> ret = response.getBody();
        assertThat(ret).isNotNull();
        assertThat(ret.containsKey("data")).isTrue();
        assertThat(ret.get("data").containsKey("clientAddress")).isTrue();
        assertThat(ret.get("data").get("clientAddress")).isEqualTo(expectedResp.get("clientAddress"));
        // eu queria checkar o ID também mas dá sempre problema pq ele compara um Long com Integer for some reason..?
    }

    @Test // DONE
    public void whenRiderHasNoCurrentOrder_testGetCurrentOrder() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.newRiderToken);
        HttpEntity<Map> entity = new HttpEntity<>(empty, headers);

        ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<Map<String, String>>() {};

        ResponseEntity<Map<String, String>> response = testRestTemplate.exchange(getBaseUrl() + "order/current", HttpMethod.GET, entity, responseType);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
        Map<String, String> ret = response.getBody();
        assertThat(ret).isNotNull();
        assertThat(ret.containsKey("data")).isTrue();
        assertThat(ret.get("data")).isEqualTo("This rider hasn't accepted an order yet");
    }

    /* *** copiado de outra branch ***
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
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusWhenInvalidId_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/1/status", HttpMethod.PATCH, entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderStatusWhenInvalidStatus_thenBadRequest() {
        Map<String, Object> empty = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        HttpEntity<Map> entity = new HttpEntity<Map>(empty, headers);

        ResponseEntity<Map> response = testRestTemplate.exchange(getBaseUrl() + "order/4/status", HttpMethod.PATCH, entity, Map.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }


    @Test
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

    */
    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/rider/"; }
}

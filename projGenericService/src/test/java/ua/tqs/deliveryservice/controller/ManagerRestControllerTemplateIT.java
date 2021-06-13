package ua.tqs.deliveryservice.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Manager;
import org.springframework.boot.test.web.client.TestRestTemplate;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ManagerRestControllerTemplateIT {
    private Manager manager;
    private Address address;
    private Rider rider;
    private Purchase purchase;
    private Store store;
    private String token;

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

    @Autowired
    private RiderRepository riderRepository;

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


    @BeforeEach
    public void beforeEachSetUp() {
        this.manager = new Manager("joao", bcryptEncoder.encode("aRightPassword"), "TQS_delivery@example.com");

        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address);

        this.rider = new Rider("Raquel", bcryptEncoder.encode("aRightPassword"), "TQS_delivery@ua.com");
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");
        this.rider.setPurchases(Arrays.asList(this.purchase));
        this.rider.setReviewsSum(4);
        this.rider.setTotalNumReviews(1);

        personRepository.saveAndFlush(this.manager);

        JwtRequest request = new JwtRequest(this.manager.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
        riderRepository.saveAndFlush(this.rider);
        purchaseRepository.saveAndFlush(this.purchase);
    }

    @AfterEach
    public void destroyAll() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        storeRepository.deleteAll();
        storeRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();
    }

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/manager/";

    }

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    public void testGetStoresWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetStoresWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetStoresButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetRidersInfo_thenOk() {
        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Map<String, Object>> riders = mapper.convertValue(
                found.get("riders"),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        Assertions.assertThat(riders).hasSize(1).extracting("name").contains(this.rider.getName());
        Assertions.assertThat(riders).hasSize(1).extracting("numberOrders").contains(1);
        Assertions.assertThat(riders).hasSize(1).extracting("average").contains(4.0);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);

    }

    @Test
    public void testGetRidersInfoWithoutResults_thenNoResults() {
        purchaseRepository.deleteAll();
        riderRepository.deleteAll();

        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Map<String, Object>> stores = mapper.convertValue(
                found.get("riders"),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        Assertions.assertThat(stores).hasSize(0);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(0);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(0);
    }
}

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
import ua.tqs.deliveryservice.repository.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ManagerRestControllerTemplateIT {
    private Manager manager;
    private Address address;
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

        personRepository.saveAndFlush(this.manager);

        JwtRequest request = new JwtRequest(this.manager.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
    }


    @AfterEach
    public void destroyAll() {
        this.deleteAll();
    }

    public String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/manager/";

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

    /* ----------------------------- *
     * GET LAST STORES (FOR MANAGER) *
     * ----------------------------- *
     */

    @Test
    public void testGetStoresWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetStoresWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetStoresButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetStores_thenStatus200() {
        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "stores", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Map<String, Object>> stores = mapper.convertValue(
                found.get("stores"),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        Assertions.assertThat(stores).hasSize(1).extracting("name").contains(this.store.getName());
        Assertions.assertThat(stores).hasSize(1).extracting("totalOrders").contains(0);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);

    }


    @Test
    public void testGetStoresNoWithoutResults_thenNoResults() {
        storeRepository.delete(this.store);
        ObjectMapper mapper = new ObjectMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "stores", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        List<Map<String, Object>> stores = mapper.convertValue(
                found.get("stores"),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        Assertions.assertThat(stores).hasSize(0);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(0);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(0);
    }

    /* ----------------------------- *
     * GET ORDERS STATISTICS         *
     * ----------------------------- *
     */

    @Test
    public void testGetStatisticsButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetStatisticsNoStores_then200() {
        storeRepository.delete(this.store);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(0);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isNull();
        Assertions.assertThat(found.get("totalStores")).isEqualTo(0);
    }

    @Test
    public void testGetStatisticsWithStoresButNoOrders_then200() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(0);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isNull();
        Assertions.assertThat(found.get("totalStores")).isEqualTo(1);
    }

    @Test
    public void testGetStatisticsWithStoresAndOrders_then200() {
        Purchase p1 = new Purchase(this.address, this.store, "Miguel");
        purchaseRepository.saveAndFlush(p1);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(1);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isNotNull();
        Assertions.assertThat(found.get("totalStores")).isEqualTo(1);
    }

    /* ----------------------------- *
     * GET RIDER STATS               *
     * ----------------------------- *
     */

    @Test
    public void testGetRiderStatsWhenUnauthorized_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "rider/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testGetRiderStatsWhenNoDeliveredPurchases_thenOKbutEmpty() {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();
        Assertions.assertThat(found.size()).isEqualTo(1);
        Assertions.assertThat(found.get("average")).isEqualTo(null);
    }

    @Test
    public void testGetRiderStatsWithDeliveredPurchases_thenOK() {
        // set up
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store);

        Purchase p1 = new Purchase(addr1, rider, store, "Miguel");
        Purchase p2 = new Purchase(addr2, rider, store, "Mariana");
        Purchase p3 = new Purchase(addr3, rider, store, "Carolina");
        p1.setStatus(Status.DELIVERED); p2.setStatus(Status.DELIVERED); p3.setStatus(Status.DELIVERED);
        p1.setDeliveryTime(264L); p2.setDeliveryTime(199L); p3.setDeliveryTime(230L);

        riderRepository.saveAndFlush(rider);
        addressRepository.saveAndFlush(addr1); addressRepository.saveAndFlush(addr2); addressRepository.saveAndFlush(addr3); addressRepository.saveAndFlush(addr_store);
        storeRepository.saveAndFlush(store);
        purchaseRepository.saveAndFlush(p1); purchaseRepository.saveAndFlush(p2); purchaseRepository.saveAndFlush(p3);

        Long expected = (p1.getDeliveryTime() + p2.getDeliveryTime() + p3.getDeliveryTime()) / 3L;

        // test
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.size()).isEqualTo(1);
        Assertions.assertThat(found.get("average")).isEqualTo(expected.intValue());
    }
}

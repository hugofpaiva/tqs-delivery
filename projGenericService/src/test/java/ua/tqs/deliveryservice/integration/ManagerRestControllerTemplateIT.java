package ua.tqs.deliveryservice.integration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ManagerRestControllerTemplateIT {
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
        this.store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address, "http://localhost:8081/delivery/");

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
        Assertions.assertThat(stores).hasSize(1).extracting("totalOrders").contains(1);

        Assertions.assertThat(found.get("currentPage")).isEqualTo(0);
        Assertions.assertThat(found.get("totalItems")).isEqualTo(1);
        Assertions.assertThat(found.get("totalPages")).isEqualTo(1);

    }


    @Test
    public void testGetStoresNoWithoutResults_thenNoResults() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
        storeRepository.delete(this.store);
        storeRepository.flush();
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
        purchaseRepository.delete(this.purchase);
        storeRepository.delete(this.store);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(0);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isEqualTo(0.0);
        Assertions.assertThat(found.get("totalStores")).isEqualTo(0);
    }

    @Test
    public void testGetStatisticsWithStoresButNoOrders_then200() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(0);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isEqualTo(0.0);
        Assertions.assertThat(found.get("totalStores")).isEqualTo(1);
    }

    @Test
    public void testGetStatisticsWithStoresAndOrders_then200() {
        Address ad1 = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(ad1);
        Purchase p1 = new Purchase(ad1, this.store, "Miguel");
        purchaseRepository.saveAndFlush(p1);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("totalPurchases")).isEqualTo(2);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isNotNull();
        Assertions.assertThat(found.get("totalStores")).isEqualTo(1);
    }

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    public void testGetRidersWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRidersWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRidersButNoAuthorization_thenUnauthorized() {
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
    public void testGetRiderStatsWhenNoDeliveredPurchases_thenOK() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();
        Assertions.assertThat(found.size()).isEqualTo(3);
        Assertions.assertThat(found.get("avgTimes")).isNull();
        Assertions.assertThat(found.get("avgReviews")).isEqualTo(4.0);
        Assertions.assertThat(found.get("inProcess")).isEqualTo(1);
    }

    @Test
    public void testGetRiderStatsWhenNoPurchases_thenOK() {
        HttpHeaders headers = new HttpHeaders();
        this.rider.setReviewsSum(0);
        this.rider.setTotalNumReviews(0);
        riderRepository.save(this.rider);
        purchaseRepository.deleteAll();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();
        Assertions.assertThat(found.size()).isEqualTo(3);
        Assertions.assertThat(found.get("avgTimes")).isNull();
        Assertions.assertThat(found.get("avgReviews")).isNull();
        Assertions.assertThat(found.get("inProcess")).isEqualTo(0);
    }

    @Test
    public void testGetRiderStatsWithDeliveredPurchases_thenOK() {
        // set up
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8081/delivery/");

        Purchase p1 = new Purchase(addr1, rider, store, "Miguel");
        Purchase p2 = new Purchase(addr2, rider, store, "Mariana");
        Purchase p3 = new Purchase(addr3, rider, store, "Carolina");
        p1.setStatus(Status.DELIVERED); p2.setStatus(Status.DELIVERED); p3.setStatus(Status.DELIVERED);
        p1.setDeliveryTime(264L); p2.setDeliveryTime(199L); p3.setDeliveryTime(230L);

        rider.setTotalNumReviews(2); rider.setReviewsSum(7);
        this.rider.setTotalNumReviews(1); this.rider.setReviewsSum(1);


        riderRepository.saveAndFlush(rider);
        riderRepository.saveAndFlush(this.rider);
        addressRepository.saveAndFlush(addr1); addressRepository.saveAndFlush(addr2); addressRepository.saveAndFlush(addr3); addressRepository.saveAndFlush(addr_store);
        storeRepository.saveAndFlush(store);
        purchaseRepository.saveAndFlush(p1); purchaseRepository.saveAndFlush(p2); purchaseRepository.saveAndFlush(p3);

        double exp_time = (double) (p1.getDeliveryTime() + p2.getDeliveryTime() + p3.getDeliveryTime()) / 3;
        double exp_rev = (7/2 + 1)/2.0 ;

        // test
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found.get("avgTimes")).isEqualTo(exp_time);
        Assertions.assertThat(found.get("avgReviews")).isEqualTo(exp_rev);
        Assertions.assertThat(found.get("inProcess")).isEqualTo(1);
    }
}

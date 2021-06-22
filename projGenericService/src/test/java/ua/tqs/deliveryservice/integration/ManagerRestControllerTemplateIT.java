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
    static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
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
    void beforeEachSetUp() {
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
    void destroyAll() {
        this.deleteAll();
    }

    String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/manager/";

    }

    void deleteAll() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        storeRepository.deleteAll();
        storeRepository.flush();

        addressRepository.deleteAll();
        addressRepository.flush();

        personRepository.deleteAll();
        personRepository.flush();

        riderRepository.deleteAll();
        riderRepository.flush();
    }

    /* ----------------------------- *
     * GET LAST STORES (FOR MANAGER) *
     * ----------------------------- *
     */

    @Test
    void testGetStoresWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetStoresWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetStoresButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "stores?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void testGetStores_thenStatus200() {
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

        Assertions.assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 1)
                .containsEntry("totalPages", 1);

    }


    @Test
    void testGetStoresNoWithoutResults_thenNoResults() {
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

        Assertions.assertThat(stores).isEmpty();

        Assertions.assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 0)
                .containsEntry("totalPages", 0);
    }

    /* ----------------------------- *
     * GET ORDERS STATISTICS         *
     * ----------------------------- *
     */

    @Test
    void testGetStatisticsButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void testGetStatisticsNoStores_then200() {
        purchaseRepository.delete(this.purchase);
        storeRepository.delete(this.store);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).containsEntry("totalPurchases", 0).containsEntry("avgPurchasesPerWeek", 0.0)
                .containsEntry("totalStores", 0);
    }

    @Test
    void testGetStatisticsWithStoresButNoOrders_then200() {
        purchaseRepository.deleteAll();
        purchaseRepository.flush();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "statistics", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).containsEntry("totalPurchases", 0).containsEntry("avgPurchasesPerWeek", 0.0)
                .containsEntry("totalStores", 1);
    }

    @Test
    void testGetStatisticsWithStoresAndOrders_then200() {
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

        Assertions.assertThat(found).containsEntry("totalPurchases", 2).containsEntry("totalStores", 1);
        Assertions.assertThat(found.get("avgPurchasesPerWeek")).isNotNull();
    }

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    void testGetRidersWhenInvalidPageNo_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageNo=" + -1, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetRidersWhenInvalidPageSize_thenBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<String> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageSize=" + 0, HttpMethod.GET, new HttpEntity<Object>(headers),
                String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetRidersButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/all?pageSize=2", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void testGetRidersInfo_thenOk() {
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

        Assertions.assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 1)
                .containsEntry("totalPages", 1);
    }

    @Test
    void testGetRidersInfoWithoutResults_thenNoResults() {
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

        Assertions.assertThat(stores).isEmpty();

        Assertions.assertThat(found).containsEntry("currentPage", 0).containsEntry("totalItems", 0)
                .containsEntry("totalPages", 0);
    }

    /* ----------------------------- *
     * GET RIDER STATS               *
     * ----------------------------- *
     */

    @Test
    void testGetRiderStatsWhenUnauthorized_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "rider/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void testGetRiderStatsWhenNoDeliveredPurchases_thenOK() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();
        Assertions.assertThat(found).hasSize(3);
        Assertions.assertThat(found.get("avgTimes")).isNull();
        Assertions.assertThat(found).containsEntry("avgReviews", 4.0).containsEntry("inProcess", 1);
    }

    @Test
    void testGetRiderStatsWhenNoPurchases_thenOK() {
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
        Assertions.assertThat(found).hasSize(3);
        Assertions.assertThat(found.get("avgTimes")).isNull();
        Assertions.assertThat(found.get("avgReviews")).isNull();
        Assertions.assertThat(found).containsEntry("inProcess", 0);
    }

    @Test
    void testGetRiderStatsWithDeliveredPurchases_thenOK() {
        // set up
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8082/delivery/");

        Purchase p1 = new Purchase(addr1, rider, store, "Miguel");
        Purchase p2 = new Purchase(addr2, rider, store, "Mariana");
        Purchase p3 = new Purchase(addr3, rider, store, "Carolina");
        p1.setStatus(Status.DELIVERED);
        p2.setStatus(Status.DELIVERED);
        p3.setStatus(Status.DELIVERED);
        p1.setDeliveryTime(264L);
        p2.setDeliveryTime(199L);
        p3.setDeliveryTime(230L);

        rider.setTotalNumReviews(2);
        rider.setReviewsSum(7);
        this.rider.setTotalNumReviews(1);
        this.rider.setReviewsSum(1);


        riderRepository.saveAndFlush(rider);
        riderRepository.saveAndFlush(this.rider);
        addressRepository.saveAndFlush(addr1);
        addressRepository.saveAndFlush(addr2);
        addressRepository.saveAndFlush(addr3);
        addressRepository.saveAndFlush(addr_store);
        storeRepository.saveAndFlush(store);
        purchaseRepository.saveAndFlush(p1);
        purchaseRepository.saveAndFlush(p2);
        purchaseRepository.saveAndFlush(p3);

        double exp_time = (double) (p1.getDeliveryTime() + p2.getDeliveryTime() + p3.getDeliveryTime()) / 3;
        double exp_rev = (7.0 / 2.0 + 1.0) / 2.0;

        // test
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/stats", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).containsEntry("avgTimes", exp_time).containsEntry("avgReviews", exp_rev)
                .containsEntry("inProcess", 1);

    }

    /* ----------------------------- *
     * GET TOP DELIVERED CITIES      *
     * ----------------------------- *
     */

    @Test
    void testGetTopDeliveredCities_thenReturn() {
        /* delete purchase from beforeEach */
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        /* set up ... */
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua das Couves, n. 51", "1234-567", "Porto", "Portugal");
        Address addr3 = new Address("Rua 31 de Dezembro, n. 12", "0000-645", "Coimbra", "Portugal");
        Address addr4 = new Address("Avenida D. Luís, n. 33", "1472-374", "Lisboa", "Portugal");
        Address addr5 = new Address("Rua São João, n. 6", "6831-353", "Guarda", "Portugal");
        Address addr6 = new Address("Rua das Marias Felizbertas, n. 28", "5830-912", "Aveiro", "Portugal");
        Address addr7 = new Address("Rua do Carmo, n. 20", "5830-912", "Porto", "Portugal");
        Address addr8 = new Address("Rua 1 de Maio, n. 8", "5830-912", "Guarda", "Portugal");
        Address addr9 = new Address("Rua peepeepoopoo, n. 1", "5830-912", "Guarda", "Portugal");
        Address addr10 = new Address("Rua das Panelas, n. 57", "5830-912", "Guarda", "Portugal");
        Address addr11 = new Address("Rua de Festa, n. 23", "5830-912", "Viseu", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http:localhost:8081");

        Purchase p1 = new Purchase(addr1, rider, store, "Miguel");
        Purchase p2 = new Purchase(addr2, rider, store, "Mariana");
        Purchase p3 = new Purchase(addr3, rider, store, "Carolina");
        Purchase p4 = new Purchase(addr4, rider, store, "Ricardo");
        Purchase p5 = new Purchase(addr5, rider, store, "Manel");
        Purchase p6 = new Purchase(addr6, rider, store, "Gustavo");
        Purchase p7 = new Purchase(addr7, rider, store, "Luana");
        Purchase p8 = new Purchase(addr8, rider, store, "Duarte");
        Purchase p9 = new Purchase(addr9, rider, store, "Hugo");
        Purchase p10 = new Purchase(addr10, rider, store, "José");
        Purchase p11 = new Purchase(addr11, rider, store, "Lucas");


        riderRepository.save(rider);
        addressRepository.saveAllAndFlush(Arrays.asList(addr1, addr2, addr3, addr4, addr5, addr6, addr7, addr8, addr9, addr10, addr11, addr_store));
        storeRepository.saveAndFlush(store);
        purchaseRepository.saveAllAndFlush(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));

        /* test ... */
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/top_delivered_cities", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).containsEntry("Lisboa", 1).containsEntry("Viseu", 1).containsEntry("Porto", 2)
                .containsEntry("Aveiro", 2).containsEntry("Guarda", 4).hasSize(5);
    }


    @Test
    void testGetTopDeliveredCities_when5DifferentCitiesDONTEXIST_thenReturn() {
        /* delete purchase from beforeEach */
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        /* set up ... */
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua das Couves, n. 51", "1234-567", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua 31 de Dezembro, n. 12", "0000-645", "Aveiro", "Portugal");
        Address addr4 = new Address("Avenida D. Luís, n. 33", "1472-374", "Aveiro", "Portugal");
        Address addr5 = new Address("Rua São João, n. 6", "6831-353", "Aveiro", "Portugal");
        Address addr6 = new Address("Rua das Marias Felizbertas, n. 28", "5830-912", "Aveiro", "Portugal");
        Address addr7 = new Address("Rua do Carmo, n. 20", "5830-912", "Aveiro", "Portugal");
        Address addr8 = new Address("Rua 1 de Maio, n. 8", "5830-912", "Guarda", "Portugal");
        Address addr9 = new Address("Rua peepeepoopoo, n. 1", "5830-912", "Guarda", "Portugal");
        Address addr10 = new Address("Rua das Panelas, n. 57", "5830-912", "Guarda", "Portugal");
        Address addr11 = new Address("Rua de Festa, n. 23", "5830-912", "Viseu", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http://localhost:8080");

        Purchase p1 = new Purchase(addr1, rider, store, "Miguel");
        Purchase p2 = new Purchase(addr2, rider, store, "Mariana");
        Purchase p3 = new Purchase(addr3, rider, store, "Carolina");
        Purchase p4 = new Purchase(addr4, rider, store, "Ricardo");
        Purchase p5 = new Purchase(addr5, rider, store, "Manel");
        Purchase p6 = new Purchase(addr6, rider, store, "Gustavo");
        Purchase p7 = new Purchase(addr7, rider, store, "Luana");
        Purchase p8 = new Purchase(addr8, rider, store, "Duarte");
        Purchase p9 = new Purchase(addr9, rider, store, "Hugo");
        Purchase p10 = new Purchase(addr10, rider, store, "José");
        Purchase p11 = new Purchase(addr11, rider, store, "Lucas");


        riderRepository.save(rider);
        addressRepository.saveAllAndFlush(Arrays.asList(addr1, addr2, addr3, addr4, addr5, addr6, addr7, addr8, addr9, addr10, addr11, addr_store));
        storeRepository.saveAndFlush(store);
        purchaseRepository.saveAllAndFlush(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11));

        /* test ... */
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/top_delivered_cities", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).containsEntry("Viseu", 1).containsEntry("Aveiro", 7).containsEntry("Guarda", 3)
                .hasSize(3);
    }

    @Test
    void testGetTopDeliveredCities_whenNoPurchases_thenReturn() {
        /* delete purchase from beforeEach */
        purchaseRepository.deleteAll();
        purchaseRepository.flush();

        /* set up ... */
        Rider rider = new Rider("Novo Rider", "a_good_password", "email@exampleTQS.com");
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua das Couves, n. 51", "1234-567", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua 31 de Dezembro, n. 12", "0000-645", "Aveiro", "Portugal");
        Address addr4 = new Address("Avenida D. Luís, n. 33", "1472-374", "Aveiro", "Portugal");
        Address addr5 = new Address("Rua São João, n. 6", "6831-353", "Aveiro", "Portugal");
        Address addr6 = new Address("Rua das Marias Felizbertas, n. 28", "5830-912", "Aveiro", "Portugal");
        Address addr7 = new Address("Rua do Carmo, n. 20", "5830-912", "Aveiro", "Portugal");
        Address addr8 = new Address("Rua 1 de Maio, n. 8", "5830-912", "Guarda", "Portugal");
        Address addr9 = new Address("Rua peepeepoopoo, n. 1", "5830-912", "Guarda", "Portugal");
        Address addr10 = new Address("Rua das Panelas, n. 57", "5830-912", "Guarda", "Portugal");
        Address addr11 = new Address("Rua de Festa, n. 23", "5830-912", "Viseu", "Portugal");

        Address addr_store = new Address("Rua ABC, n. 922", "4444-555", "Puerto", "Portugal");
        Store store = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store, "http:localhost:8080");


        riderRepository.save(rider);
        addressRepository.saveAllAndFlush(Arrays.asList(addr1, addr2, addr3, addr4, addr5, addr6, addr7, addr8, addr9, addr10, addr11, addr_store));
        storeRepository.saveAndFlush(store);

        /* test ... */
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "riders/top_delivered_cities", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));


        Map<String, Object> found = response.getBody();
        Assertions.assertThat(found).isEmpty();

    }
}

package ua.tqs.deliveryservice.integration;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.exception.UnreachableServiceException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;
import ua.tqs.deliveryservice.services.RiderService;
import ua.tqs.deliveryservice.specific.ISpecificService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

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

        this.rider = new Rider("Joao", bcryptEncoder.encode("aRightPassword"), "TQS_delivery@example.com");
        personRepository.saveAndFlush(this.rider);

        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(this.address);

        Address purchAddres = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        addressRepository.saveAndFlush(purchAddres);

        this.store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", this.address, "http://localhost:8080/delivery/", 1.0, 1.0);

        this.purchase = new Purchase(purchAddres, this.rider, this.store, "Joana");


        JwtRequest request = new JwtRequest(this.rider.getEmail(), "aRightPassword");
        ResponseEntity<Map> response = testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/login", request, Map.class);
        this.token = response.getBody().get("token").toString();

      
        storeRepository.saveAndFlush(this.store);
        purchaseRepository.saveAndFlush(this.purchase);
    }

    @AfterEach
    public void destroyAll() {
        this.deleteAll();
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


    /* ----------------------------- *
     * GET ORDER HISTORY FOR RIDER   *
     * ----------------------------- *
     */

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

    /* ----------------------------- *
     * GET CURRENT PURCHASE OF RIDER *
     * ----------------------------- *
     */


    @Test
    public void whenRiderHasCurrentOrderButNoAuthorization_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/current", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void whenRiderHasNoCurrentOrder_whenGetCurrentOrder_get404() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        purchaseRepository.delete(this.purchase);

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/current", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void whenRiderHasCurrentOrder_testGetCurrentOrder() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/current", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.containsKey("data")).isTrue();

        Map<String, Object> info = mapper.convertValue(
                found.get("data"),
                new TypeReference<Map<String, Object>>() {
                }
        );

        Assertions.assertThat(info.containsKey("clientAddress")).isTrue();
        Assertions.assertThat(info.containsKey("orderId")).isTrue();
        Assertions.assertThat(info.get("status")).isEqualTo("ACCEPTED");
    }

    /* ----------------------------- *
     * GET NEW PURCHASE FOR RIDER    *
     * ----------------------------- *
     */


    @Test
    public void givenRiderHasNoAuthorization_whenGetNewPurchase_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void whenRiderHasCurrentOrder_whenGetNewOrder_thenForbidden() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }

    @Test
    public void givenThereAreNoOrders_whenGetNewOrder_thenNotFound() {
        purchaseRepository.delete(this.purchase);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void givenRiderHasNoOrder_whenGetNewOrder_thenGetNewOrder() {
        purchaseRepository.delete(this.purchase);


        Purchase p = new Purchase(address, store, "Joana");
        this.purchase = purchaseRepository.saveAndFlush(p);
        System.out.println(p.getId());
        System.out.println(this.purchase.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.containsKey("data")).isTrue();

        Map<String, Object> info = mapper.convertValue(
                found.get("data"),
                new TypeReference<Map<String, Object>>() {
                }
        );

        Assertions.assertThat(info.containsKey("clientAddress")).isTrue();
        Assertions.assertThat(info.containsKey("orderId")).isTrue();
        Assertions.assertThat(info.get("status")).isEqualTo("ACCEPTED");
    }


    /* ----------------------------- *
     * UPDATE STATUS OF PURCHASE     *
     * ----------------------------- *
     */

    @Test
    public void givenRiderHasNoAuthorization_whenUpdatePurchase_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/status", HttpMethod.PUT, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }


    @Test
    public void givenRiderHasNoCurrentOrder_whenUpdatePurchase_get404() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        purchaseRepository.delete(this.purchase);

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/status", HttpMethod.PUT, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void givenRiderHasCurrentOrder_whenUpdatePurchaseStatus_thenSuccess() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        System.out.println(purchase.getId());

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/status", HttpMethod.PUT, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.containsKey("order_id")).isTrue();
        Assertions.assertThat(found.containsKey("status")).isTrue();
        Assertions.assertThat(found.containsKey("delivery_time")).isFalse();

        Assertions.assertThat(found.get("status")).isEqualTo("PICKED_UP");
    }

    @Test
    public void givenRiderHasCurrentOrder_whenUpdatePurchaseStatusIsPickedUp_thenSuccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        this.purchase.setStatus(Status.PICKED_UP);
        purchaseRepository.saveAndFlush(this.purchase);

        System.out.println(purchase.getId());

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/status", HttpMethod.PUT, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.containsKey("order_id")).isTrue();
        Assertions.assertThat(found.containsKey("status")).isTrue();
        Assertions.assertThat(found.containsKey("delivery_time")).isTrue();

        Assertions.assertThat(found.get("status")).isEqualTo("DELIVERED");
    }


    /* ----------------------------- *
     * GET RIDER REVIEW STATISTICS   *
     * ----------------------------- *
     */




    @Test
    public void givenRiderHasNoAuthorization_whenGetReviewsStatistics_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "reviews", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void givenRiderWithoutReviews_whenGetReviewsStatistics_thenReturnStatistics() {
        HttpHeaders headers = new HttpHeaders();

        System.out.println(purchase.getId());
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "reviews", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        Map<String, Object> found = response.getBody();
        assertThat(found.get("totalNumReviews"), equalTo(0));
        assertThat(found.get("avgReviews"), nullValue());
    }

    @Test
    public void givenRiderWithReviews_whenGetReviewsStatistics_thenReturnStatistics() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);

        this.rider.setTotalNumReviews(4);
        this.rider.setReviewsSum(15);
        personRepository.saveAndFlush(this.rider);

        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "reviews", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        Map<String, Object> found = response.getBody();
        assertThat(found.get("totalNumReviews"), equalTo(4));
        assertThat(found.get("avgReviews"), equalTo((double) 15/4));
    }



    /* ------------------------------------- *
     * GET NEW PURCHASE FOR RIDER WITH LOC   *
     * ------------------------------------- *
     */

    @Test
    public void givenRiderHasNoAuthorization_whenGetNewPurchaseWithLoc_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new?latitude=30.2312&longitude=50.234", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        System.out.println(purchase.getId());
        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
    }
    @Test
    public void whenRiderHasCurrentOrder_whenGetNewOrderWithLoc_thenForbidden() {
        HttpHeaders headers = new HttpHeaders();
        System.out.println(purchase.getId());
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new?latitude=30.2312&longitude=50.234", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }

    @Test
    public void givenThereAreNoOrders_whenGetNewOrderWithLoc_thenNotFound() {
        purchaseRepository.delete(this.purchase);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new?latitude=30.2312&longitude=50.234", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }


    @Test
    public void givenThereAreOrders_whenGetNewOrderWithInvalidLoc_then400() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new?latitude=2.2312&longitude=222.234", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void givenRiderHasNoOrder_whenGetNewOrderWithLoc_thenGetClosestOrder() {
        purchaseRepository.delete(this.purchase);

        Address addr_store_far = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Store store_far = new Store("Loja do Manel", "A melhor loja.", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODg5OX0.tNilyrTKno-BY118_2wmzwpPAWVxo-14R7U8WUPozUFx0yDKJ-5iPrhaNg-NXmiEqZa8zfcL_1gVrjHNX00V7g", addr_store_far,"http://localhost:8079/delivery/", 5.0, 5.0);

        Address addr_store_close = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");


        Address addr_far = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_far = new Purchase(addr_far, store_far, "Miguel");

        Address addr_close = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Purchase p1_close = new Purchase(addr_close, store, "Miguel");

        addressRepository.save(addr_store_far); addressRepository.save(addr_store_close); addressRepository.save(addr_close); addressRepository.save(addr_far);
        storeRepository.save(store); storeRepository.save(store_far);
        purchaseRepository.save(p1_far); purchaseRepository.save(p1_close);


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.token);
        ResponseEntity<Map> response = testRestTemplate.exchange(
                getBaseUrl() + "order/new?latitude=1.04&longitude=0.234", HttpMethod.GET, new HttpEntity<Object>(headers),
                Map.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> found = response.getBody();

        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.containsKey("data")).isTrue();

        Map<String, Object> info = mapper.convertValue(
                found.get("data"),
                new TypeReference<Map<String, Object>>() {
                }
        );

        Assertions.assertThat(info.containsKey("store")).isTrue();
        Assertions.assertThat(((Map<String, Object>)info.get("store")).containsKey("latitude")).isTrue();
        Assertions.assertThat(((Map<String, Object>)info.get("store")).get("latitude")).isEqualTo(store.getLatitude());
        Assertions.assertThat(info.containsKey("clientAddress")).isTrue();
        Assertions.assertThat(info.containsKey("orderId")).isTrue();
        Assertions.assertThat(info.get("status")).isEqualTo("ACCEPTED");
    }

}

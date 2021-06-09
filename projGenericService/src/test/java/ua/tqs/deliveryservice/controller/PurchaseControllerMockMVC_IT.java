package ua.tqs.deliveryservice.controller;


import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectWriter;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;
import ua.tqs.deliveryservice.model.*;

import ua.tqs.deliveryservice.repository.AddressRepository;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PurchaseControllerMockMVC_IT {
    @Autowired
    private MockMvc mvc;

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
    public void setUp(){
        this.rider = new Rider("TQS_delivery@example.com", bcryptEncoder.encode("aRightPassword"), "Joao");
        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");

        personRepository.saveAndFlush(this.rider);
        addressRepository.saveAndFlush(this.address);
        storeRepository.saveAndFlush(this.store);
        purchaseRepository.saveAndFlush(this.purchase);
    }

    // verifica todas as verificações que o controller tem de fazer + verifica se no final
    // a entidade (neste caso de patch nao se retorna mas) se o status está certo
    // exemplo de mock com patch: https://www.logicbig.com/tutorials/spring-framework/spring-web-mvc/http-patch-test.html
    @Test
    public void testNullOrderId_thenBadRequest() throws Exception {
        // no mock mvc consigo chegar ao /rider sem despoletar erro de unauthorized, o que não acontece
        // com os testes de integração com o IT porque lá usam-se os serviços reais e não há maneira de o rider chegar ali sem estar autenticado
        JSONObject json = new JSONObject();
        json.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + this.store.getToken());

        mvc.perform( patch(getBaseUrl() + "/order/" + null + "/review")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers)
                    .content(String.valueOf(json)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void testNullReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " +  this.store.getToken());

        mvc.perform( patch(getBaseUrl() + "/order/" + this.purchase.getId() + "/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidMinReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", -1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + this.store.getToken());

        mvc.perform( patch(getBaseUrl() + "/order/" + this.purchase.getId() + "/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidMaxReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " +  this.store.getToken());

        mvc.perform( patch(getBaseUrl() + "/order/" + this.purchase.getId() + "/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEverythingOK_thenIsOk() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 4);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " +  this.store.getToken());

        mvc.perform( patch(getBaseUrl() + "/order/" + this.purchase.getId() + "/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isOk());
    }

    public String getBaseUrl() { return "http://localhost:" + randomServerPort + "/store"; }
}

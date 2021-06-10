package ua.tqs.deliveryservice.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.deliveryservice.configuration.JwtRequestFilter;
import ua.tqs.deliveryservice.configuration.WebSecurityConfig;
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.services.PurchaseService;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = RiderRestController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class PurchaseRestControllerMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private Rider rider;
    private Address address;
    private Store store;
    private Purchase purchase;

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    void setUp() {
        this.rider = new Rider("Joao", "aRightPassword", "TQS_delivery@example.com");
        this.address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        this.store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", this.address);
        this.purchase = new Purchase(this.address, this.rider, this.store, "Joana");
    }

    @Disabled
    @WithMockUser(username = "HumberPecas", password = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", roles = "Store")
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

        Mockito.verify(purchaseService, VerificationModeFactory.times(0)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Disabled
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

    @Disabled
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

    @Disabled
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

    @Disabled
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

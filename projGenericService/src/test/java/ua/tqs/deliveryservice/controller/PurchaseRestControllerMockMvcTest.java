package ua.tqs.deliveryservice.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.deliveryservice.configuration.JwtRequestFilter;
import ua.tqs.deliveryservice.configuration.WebSecurityConfig;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.services.PurchaseService;


import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = PurchaseRestController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class PurchaseRestControllerMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";

    // 1. despoletar erros no controller e ver se o service não é chamado, se quer
    @Test
    public void testNullOrderId_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 3L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization",  "Bearer " + token);

        mvc.perform( put("/store/order/" + null + "/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(0)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testNullReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization",  "Bearer " + token);

        mvc.perform( put("/store/order/3/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(0)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testInvalidMinReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", -1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + token);

        mvc.perform( put("/store/order/3/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(0)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testInvalidMaxReview_thenBadRequest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        headers.set("authorization", "Bearer " + token);

        mvc.perform( put("/store/order/3/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(0)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testNotDeliveredPurchaseReview_thenBadRequest() throws Exception {
        Rider rider = new Rider("Joao", "aRightPassword", "TQS_delivery@example.com");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", address);
        Purchase purchase = new Purchase(address, rider, store, "Joana");

        JSONObject json = new JSONObject();
        json.put("review", 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + token);

        when(purchaseService.reviewRiderFromSpecificOrder(token, purchase.getId(), 3)).thenThrow(InvalidValueException.class);

        mvc.perform(put("/store/order/"+purchase.getId()+"/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(1)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    // 2. despoletar erros no service e ver se o controller ainda faz o que é suposto
    @Test
    public void testEverythingOK_thenIsOk() throws Exception {
        Rider rider = new Rider("Joao", "aRightPassword", "TQS_delivery@example.com");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw", address);
        Purchase purchase = new Purchase(address, rider, store, "Joana");
        purchase.setStatus(Status.DELIVERED);

        JSONObject json = new JSONObject();
        json.put("review", 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + token);

        when(purchaseService.reviewRiderFromSpecificOrder(token, purchase.getId(), 3)).thenReturn(purchase);

        mvc.perform(put("/store/order/"+purchase.getId()+"/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isOk());

        Mockito.verify(purchaseService, VerificationModeFactory.times(1)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testPurchaseNotFound_thenResourceNotFound() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + token);

        when(purchaseService.reviewRiderFromSpecificOrder(token, -1L, 3)).thenThrow(ResourceNotFoundException.class);

        mvc.perform( put("/store/order/-1/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isNotFound());

        Mockito.verify(purchaseService, VerificationModeFactory.times(1)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testStoreNotFound_thenUnauthorized() throws Exception {
        JSONObject json = new JSONObject();
        json.put("review", 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearererer " + token);

        when(purchaseService.reviewRiderFromSpecificOrder( "rer " + token, 3L, 3)).thenThrow(InvalidLoginException.class);

        mvc.perform( put("/store/order/3/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(purchaseService, VerificationModeFactory.times(1)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    @Test
    public void testWhenPurchaseAlreadyHas_thenBadRequest() throws Exception {
        // this can either happen when the purchase already has a review or the store where the purchase was made is
        // not the same that has the token passed.
        JSONObject json = new JSONObject();
        json.put("review", 3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("authorization", "Bearer " + token);

        when(purchaseService.reviewRiderFromSpecificOrder(token, 3L, 3)).thenThrow(InvalidValueException.class);

        mvc.perform( put("/store/order/3/review")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(String.valueOf(json)))
                .andExpect(status().isBadRequest());

        Mockito.verify(purchaseService, VerificationModeFactory.times(1)).reviewRiderFromSpecificOrder(anyString(), anyLong(), anyInt());
    }

    /* ----------------------------- *
     * CLIENT MAKES NEW ORDER TESTS  *
     * ----------------------------- *
     */

    @Test
    public void testMakeNewOrderButNoStoreAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String exampleBody = "{ \"personName\" : \"nomeee\", \"date\" : 1623709488744, \"address\" : {\"address\" : \"Rua 1123\", \"postalCode\" : \"3423-234\", \"city\" : \"aveiro\", \"country\" : \"pt\" }}}";
        when(purchaseService.receiveNewOrder(any(), any())).thenThrow(InvalidLoginException.class);

        mvc.perform(post("/store/order")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(exampleBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
        ;

        verify(purchaseService, times(1)).receiveNewOrder(any(), any());
    }

    @Test
    public void testMakeNewOrderButFieldMissing_then400() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String exampleBody = "{\"date\":1623709488744,\"address\":{\"address\":\"Rua1123\",\"postalCode\":\"3423-234\",\"city\":\"aveiro\",\"country\":\"pt\"}}";
        when(purchaseService.receiveNewOrder(any(), any())).thenThrow(InvalidValueException.class);
        mvc.perform(post("/store/order")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(exampleBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
        verify(purchaseService, times(1)).receiveNewOrder(any(), any());
    }

    @Test
    public void testMakeNewOrderButBadField_then400() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);

        String exampleBody = "{\"personName\":234,\"date\":1623709488744,\"address\":{\"address\":\"Rua1123\",\"postalCode\":\"3423-234\",\"city\":\"aveiro\",\"country\":\"pt\"}}";
        when(purchaseService.receiveNewOrder(any(), any())).thenThrow(InvalidValueException.class);
        mvc.perform(post("/store/order")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(exampleBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;
        verify(purchaseService, times(1)).receiveNewOrder(any(), any());
    }

    @Test
    public void testMakeNewOrderGood_then200() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + token);

        String exampleBody = "{\"personName\":\"nomeee\",\"date\":1623709488744,\"address\":{\"address\":\"Rua1123\",\"postalCode\":\"3423-234\",\"city\":\"aveiro\",\"country\":\"pt\"}}";
        Purchase purchase = new Purchase(new Address("Rua1123", "3423-234", "aveiro", "pt"), new Store(), "nomeee");
        when(purchaseService.receiveNewOrder(any(), any())).thenReturn(purchase);

        mvc.perform(post("/store/order")
                .accept(MediaType.APPLICATION_JSON)
                .headers(headers)
                .content(exampleBody)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("orderId").exists())
        ;
        verify(purchaseService, times(1)).receiveNewOrder(any(), any());
    }

}

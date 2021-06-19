package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.deliveryservice.configuration.JwtRequestFilter;
import ua.tqs.deliveryservice.configuration.WebSecurityConfig;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.services.PurchaseService;
import ua.tqs.deliveryservice.services.RiderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Disables Security
@WebMvcTest(value = RiderRestController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class RiderRestControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    @MockBean
    private RiderService riderService;

    // Although Spring Security is disabled, Spring Context will still check WebConfig.
    // Without a mock of the Autowire there, it will fail
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    /* ----------------------------- *
     * GET ORDER HISTORY FOR RIDER   *
     * ----------------------------- *
     */

    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageNo_thenBadRequest() throws Exception {
        mvc.perform(get("/rider/orders")
                .param("pageNo", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(purchaseService, times(0)).getLastOrderForRider(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageSize_thenBadRequest() throws Exception {
        mvc.perform(get("/rider/orders")
                .param("pageSize", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(purchaseService, times(0)).getLastOrderForRider(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetRiderOrderHistory_thenStatus200() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");
        List<Purchase> purchases = new ArrayList<>();
        purchases.add(purchase);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 1);
        response.put("totalPages", 1);
        response.put("orders", purchases);

        when(purchaseService.getLastOrderForRider(0, 10, "Bearer example_token")).thenReturn(response);

        mvc.perform(get("/rider/orders")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(1)))
                .andExpect(jsonPath("totalPages", is(1)))
                .andExpect(jsonPath("['orders'].size()", is(1)))
                .andExpect(jsonPath("['orders'][0].id", is(((Long) purchase.getId()).intValue())));

        verify(purchaseService, times(1)).getLastOrderForRider(0, 10, "Bearer example_token");
    }

    @Test
    public void testGetRiderOrderHistoryPageNoWithoutResults_thenNoResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        List<Purchase> purchases = new ArrayList<>();

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 0);
        response.put("totalPages", 0);
        response.put("orders", purchases);

        when(purchaseService.getLastOrderForRider(0, 10, "Bearer example_token")).thenReturn(response);

        mvc.perform(get("/rider/orders")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(0)))
                .andExpect(jsonPath("totalPages", is(0)))
                .andExpect(jsonPath("['orders'].size()", is(0)));

        verify(purchaseService, times(1)).getLastOrderForRider(0, 10, "Bearer example_token");
    }

    @Test
    public void testGetRiderOrderHistoryPageNoAndLimitedPageSize_thenLimitedResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");

        Address address1 = new Address("Universidade de Lisboa", "3800-000", "Aveiro", "Portugal");
        Purchase purchase1 = new Purchase(address1, rider, store, "João");

        List<Purchase> purchases = new ArrayList<>();
        purchases.add(purchase);
        purchases.add(purchase1);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 3);
        response.put("totalPages", 2);
        response.put("orders", purchases);

        when(purchaseService.getLastOrderForRider(0, 2, "Bearer example_token")).thenReturn(response);

        mvc.perform(get("/rider/orders")
                .param("pageSize", String.valueOf(2))
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(3)))
                .andExpect(jsonPath("totalPages", is(2)))
                .andExpect(jsonPath("['orders'].size()", is(2)))
                .andExpect(jsonPath("['orders'][0].id", is(((Long) purchase.getId()).intValue())))
                .andExpect(jsonPath("['orders'][1].id", is(((Long) purchase1.getId()).intValue())));

        verify(purchaseService, times(1)).getLastOrderForRider(0, 2, "Bearer example_token");
    }

    @Test
    public void testGetRiderOrderHistoryButNoAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getLastOrderForRider(0, 10, "Bearer example_token")).thenThrow(InvalidLoginException.class);

        mvc.perform(get("/rider/orders")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(purchaseService, times(1)).getLastOrderForRider(0, 10, "Bearer example_token");
    }

    /* ----------------------------- *
     * GET CURRENT PURCHASE OF RIDER *
     * ----------------------------- *
     */

    @Test
    public void testGetCurrentPurchaseButNoAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getCurrentPurchase("Bearer example_token")).thenThrow(InvalidLoginException.class);
        mvc.perform(get("/rider/order/current")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
        ;

        verify(purchaseService, times(1)).getCurrentPurchase(any());
    }

    @Test
    public void givenRiderWithoutPurchase_whenGetCurrentPurchase_then404() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getCurrentPurchase("Bearer example_token")).thenThrow(ResourceNotFoundException.class);

        mvc.perform(get("/rider/order/current")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(purchaseService, times(1)).getCurrentPurchase(any());
    }

    @Test
    public void givenRiderWithPurchase_whenGetCurrentPurchase_then200() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");

        when(purchaseService.getCurrentPurchase("Bearer example_token")).thenReturn(purchase);

        mvc.perform(get("/rider/order/current")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.orderId", is(((Long) purchase.getId()).intValue())))
                .andExpect(jsonPath("data.clientName", is(purchase.getClientName())))
                .andExpect(jsonPath("data.status", is("ACCEPTED")))
        ;

        verify(purchaseService, times(1)).getCurrentPurchase(any());
    }


    /* ----------------------------- *
     * GET NEW PURCHASE FOR RIDER    *
     * ----------------------------- *
     */

    @Test
    public void testGetNewPurchaseButNoAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getNewPurchase("Bearer example_token")).thenThrow(InvalidLoginException.class);
        mvc.perform(get("/rider/order/new")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
        ;

        verify(purchaseService, times(1)).getNewPurchase(any());
    }

    @Test
    public void givenRiderWithPurchase_whenGetNewPurchase_thenForbidden() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getNewPurchase("Bearer example_token")).thenThrow(ForbiddenRequestException.class);
        mvc.perform(get("/rider/order/new")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
        ;

        verify(purchaseService, times(1)).getNewPurchase(any());
    }

    @Test
    public void givenNoMorePurchases_whenGetNewPurchase_then404() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.getNewPurchase("Bearer example_token")).thenThrow(ResourceNotFoundException.class);
        mvc.perform(get("/rider/order/new")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
        ;
        verify(purchaseService, times(1)).getNewPurchase(any());
    }

    @Test
    public void givenRiderWithoutPurchase_whenGetNewPurchase_thenGetPurchase() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");

        when(purchaseService.getNewPurchase("Bearer example_token")).thenReturn(purchase);

        mvc.perform(get("/rider/order/new")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.orderId", is(((Long) purchase.getId()).intValue())))
                .andExpect(jsonPath("data.clientName", is(purchase.getClientName())))
                .andExpect(jsonPath("data.status", is("ACCEPTED")))
        ;

        verify(purchaseService, times(1)).getNewPurchase(any());
    }

    /* ----------------------------- *
     * UPDATE PURCHASE STATUS        *
     * ----------------------------- *
     */

    @Test
    public void testUpdateStatusButNoAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.updatePurchaseStatus("Bearer example_token")).thenThrow(InvalidLoginException.class);
        mvc.perform(patch("/rider/order/status")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
        ;

        verify(purchaseService, times(1)).updatePurchaseStatus(any());
    }


    @Test
    public void givenRiderWithoutPurchase_whenUpdatePurchaseStatus_then404() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(purchaseService.updatePurchaseStatus("Bearer example_token")).thenThrow(ResourceNotFoundException.class);

        mvc.perform(patch("/rider/order/status")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(purchaseService, times(1)).updatePurchaseStatus(any());
    }


    @Test
    public void givenRiderWithPurchase_whenUpdateStatus_thenSuccess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");
        purchase.setStatus(Status.PICKED_UP);

        when(purchaseService.updatePurchaseStatus("Bearer example_token")).thenReturn(purchase);


        mvc.perform(patch("/rider/order/status")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("order_id", is(((Long) purchase.getId()).intValue())))
                .andExpect(jsonPath("status", is("PICKED_UP")))
        ;

        verify(purchaseService, times(1)).updatePurchaseStatus(any());

    }

    @Test
    public void givenRiderWithPurchase_whenUpdateStatusAndStatusWasPickedUp_thenSuccess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Rider rider = new Rider("TQS_delivery@example.com", "aRightPassword", "Joao");
        Address address = new Address("Universidade de Aveiro", "3800-000", "Aveiro", "Portugal");
        Store store = new Store("HumberPecas", "Peça(s) rápido", "somestringnewtoken", address, "http://localhost:8081/delivery/");
        Purchase purchase = new Purchase(address, rider, store, "Joana");
        purchase.setStatus(Status.PICKED_UP);
        purchase.setDeliveryTime(15L);

        when(purchaseService.updatePurchaseStatus("Bearer example_token")).thenReturn(purchase);


        mvc.perform(patch("/rider/order/status")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("order_id", is(((Long) purchase.getId()).intValue())))
                .andExpect(jsonPath("status", is("PICKED_UP")))
                .andExpect(jsonPath("delivery_time", is(15)));

        verify(purchaseService, times(1)).updatePurchaseStatus(any());

    }

    /* ----------------------------- *
     * GET RIDER REVIEW STATISTICS   *
     * ----------------------------- *
     */

    @Test
    public void testGetReviewStatisticsButNoAuthorization_thenUnauthorized() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        when(riderService.getRatingStatistics("Bearer example_token")).thenThrow(InvalidLoginException.class);
        mvc.perform(get("/rider/reviews")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
        ;

        verify(riderService, times(1)).getRatingStatistics(any());
    }


    @Test
    public void givenRiderWithoutReviews_whenGetStatistics_thenSuccess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Map<String, Object> expected = new HashMap<>();
        expected.put("totalNumReviews", 0);
        expected.put("avgReviews", null);

        when(riderService.getRatingStatistics("Bearer example_token")).thenReturn(expected);


        mvc.perform(get("/rider/reviews")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalNumReviews", is(0)))
                .andExpect(jsonPath("avgReviews").isEmpty())
        ;

        verify(riderService, times(1)).getRatingStatistics(any());

    }

    @Test
    public void givenRiderWithReviews_whenGetStatistics_thenSuccess() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Map<String, Object> expected = new HashMap<>();
        expected.put("totalNumReviews", 5);
        expected.put("avgReviews", 3.5);

        when(riderService.getRatingStatistics("Bearer example_token")).thenReturn(expected);


        mvc.perform(get("/rider/reviews")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalNumReviews", is(5)))
                .andExpect(jsonPath("avgReviews", is(3.5)))
        ;

        verify(riderService, times(1)).getRatingStatistics(any());

    }
}
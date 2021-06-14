package ua.tqs.deliveryservice.controller;


import com.github.dockerjava.api.exception.UnauthorizedException;
import com.sun.source.tree.Tree;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ua.tqs.deliveryservice.configuration.JwtRequestFilter;
import ua.tqs.deliveryservice.configuration.WebSecurityConfig;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.services.PurchaseService;
import ua.tqs.deliveryservice.services.StoreService;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Disables Security
@WebMvcTest(value = ManagerRestController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class ManagerRestControllerMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private PurchaseService purchaseService;


    // Although Spring Security is disabled, Spring Context will still check WebConfig.
    // Without a mock of the Autowire there, it will fail
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    /* ----------------------------- *
     * GET STORES (FOR MANAGER)      *
     * ----------------------------- *
     */


    @Test
    public void testGetStoresWhenInvalidPageNo_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/stores")
                .param("pageNo", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(storeService, times(0)).getStores(Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetStoresWhenInvalidPageSize_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/stores")
                .param("pageSize", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(storeService, times(0)).getStores(Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetStores_thenStatus200() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");


        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 944", "4444-555", "Aveiro", "Portugal");
        Map<String, Object> store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1).getMap();
        store1.put("totalOrders", 1);
        Map<String, Object> store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2).getMap();
        store2.put("totalOrders", 2);
        Map<String, Object> store3 = new Store("Loja do Manel33", "A melhor loja.", "exToken3", addr3).getMap();
        store3.put("totalOrders", 3);

        List<Map<String, Object>> stores = Arrays.asList(store1, store2, store3);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 3);
        response.put("totalPages", 1);
        response.put("stores", stores);

        when(storeService.getStores(0, 10)).thenReturn(response);

        mvc.perform(get("/manager/stores")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(3)))
                .andExpect(jsonPath("totalPages", is(1)))
                .andExpect(jsonPath("['stores'].size()", is(3)))
                .andExpect(jsonPath("['stores'][0].id", is(((Long) store1.get("id")).intValue())))
                .andExpect(jsonPath("['stores'][2].totalOrders", is(store3.get("totalOrders"))));

        verify(storeService, times(1)).getStores(0, 10);
    }


    @Test
    public void testGetStoresPageNoAndLimitedPageSize_thenLimitedResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");
        Address addr3 = new Address("Rua ABC, n. 944", "4444-555", "Aveiro", "Portugal");
        Map<String, Object> store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1).getMap();
        store1.put("totalOrders", 1);
        Map<String, Object> store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2).getMap();
        store2.put("totalOrders", 2);

        List<Map<String, Object>> stores = Arrays.asList(store1, store2);

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 3);
        response.put("totalPages", 2);
        response.put("stores", stores);

        when(storeService.getStores(0, 2)).thenReturn(response);

        mvc.perform(get("/manager/stores")
                .param("pageSize", String.valueOf(2))
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(3)))
                .andExpect(jsonPath("totalPages", is(2)))
                .andExpect(jsonPath("['stores'].size()", is(2)))
                .andExpect(jsonPath("['stores'][0].id", is(((Long) store1.get("id")).intValue())))
                .andExpect(jsonPath("['stores'][1].totalOrders", is( store2.get("totalOrders") )));

        verify(storeService, times(1)).getStores(0, 2);
    }

    @Test
    public void testGetStorePageNoWithoutResults_thenNoResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        List<Store> stores = new ArrayList<>();

        Map<String, Object> response = new TreeMap<>();
        response.put("currentPage", 0);
        response.put("totalItems", 0);
        response.put("totalPages", 0);
        response.put("stores", stores);

        when(storeService.getStores(0, 10)).thenReturn(response);

        mvc.perform(get("/manager/stores")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(0)))
                .andExpect(jsonPath("totalPages", is(0)))
                .andExpect(jsonPath("['stores'].size()", is(0)));

        verify(storeService, times(1)).getStores(0, 10);
    }


    /* ----------------------------- *
     * GET STATISTICS (FOR MANAGER)  *
     * ----------------------------- *
     */


    @Test
    public void testGetStatisticsWithoutAnyStore_thenNoResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        TreeMap<String, Object> response = new TreeMap<>();
        response.put("totalPurchases", 0);
        response.put("avgPurchasesPerWeek", null);
        response.put("totalStores", 0);

        when(storeService.getStatistics()).thenReturn(response);

        mvc.perform(get("/manager/statistics")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("totalPurchases", is(0)))
                .andExpect(jsonPath("avgPurchasesPerWeek").isEmpty())
                .andExpect(jsonPath("totalStores", is(0)));

        verify(storeService, times(1)).getStatistics();
    }

    @Test
    public void testGetStatisticsWithStores_thenResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        TreeMap<String, Object> response = new TreeMap<>();
        response.put("totalPurchases", 45);
        response.put("avgPurchasesPerWeek", 3.4323);
        response.put("totalStores", 2);

        when(storeService.getStatistics()).thenReturn(response);

        mvc.perform(get("/manager/statistics")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("totalPurchases", is(response.get("totalPurchases"))))
                .andExpect(jsonPath("avgPurchasesPerWeek", is(response.get("avgPurchasesPerWeek"))))
                .andExpect(jsonPath("totalStores", is(response.get("totalStores"))));

        verify(storeService, times(1)).getStatistics();
    }

    /* ----------------------------- *
     * GET RIDER STATS               *
     * ----------------------------- *
     */

    @Test
    public void getRidersStatsWhenNoDeliveredPurchases_thenNoResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Map<String, Object> response = new HashMap<>();
        response.put("average", null);
        when(purchaseService.getAvgDeliveryTime()).thenReturn(response);

        mvc.perform(get("/manager/riders/stats")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("average").doesNotExist());

        verify(purchaseService, times(1)).getAvgDeliveryTime();
    }

    @Test
    public void getRidersStatsWithDeliveredPurchases_thenResults() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + "example_token");

        Map<String, Object> response = new HashMap<>();
        response.put("average", 231);
        when(purchaseService.getAvgDeliveryTime()).thenReturn(response);

        mvc.perform(get("/manager/riders/stats")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("average", is(response.get("average"))));

        verify(purchaseService, times(1)).getAvgDeliveryTime();
    }

}

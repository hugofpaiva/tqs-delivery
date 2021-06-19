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
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.services.ManagerService;
import ua.tqs.deliveryservice.services.PurchaseService;
import ua.tqs.deliveryservice.services.StoreService;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private ManagerService managerService;

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
        Map<String, Object> store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1, "http://localhost:8081/delivery/").getMap();
        store1.put("totalOrders", 1);
        Map<String, Object> store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2, "http://localhost:8082/delivery/").getMap();
        store2.put("totalOrders", 2);
        Map<String, Object> store3 = new Store("Loja do Manel33", "A melhor loja.", "exToken3",  addr3, "http://localhost:8083/delivery/").getMap();
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
        Map<String, Object> store1 = new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1, "http://localhost:8081/delivery/").getMap();
        store1.put("totalOrders", 1);
        Map<String, Object> store2 = new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2, "http://localhost:8082/delivery/").getMap();
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

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    public void testGetAllRidersInformationInvalidPageNo_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "bad_token")
                .param("pageNo", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(managerService, times(0)).getRidersInformation(any(), any());
    }

    @Test
    public void testGetAllRidersInformationInvalidPageSize_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "bad_token")
                .param("pageSize", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(managerService, times(0)).getRidersInformation(any(), any());
    }

    @Test
    public void testGetAllRidersInformationEverythingValid_thenOk() throws Exception {
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");

        Rider rider1 = new Rider("Rider 1", "goodpwd", "rider1@ua.pt");
        Rider rider2 = new Rider("Rider 2", "goodpwd", "rider2@ua.pt");

        Purchase purchase1 = new Purchase(addr1, rider1, new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1, "http://localhost:8081/delivery/"), "Adelaide");
        Purchase purchase2 = new Purchase(addr2, rider1,  new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2, "http://localhost:8082/delivery/"), "Rita");

        rider1.setReviewsSum(7); rider1.setTotalNumReviews(2); rider1.setPurchases(Arrays.asList(purchase1, purchase2));

        Map<String, Object> rider1Info = new HashMap<>();
        rider1Info.put("name", rider1.getName());
        rider1Info.put("numberOrders", rider1.getPurchases().size());
        rider1Info.put("average", rider1.getReviewsSum() * 1.0 / rider1.getTotalNumReviews());

        Map<String, Object> rider2Info = new HashMap<>();
        rider2Info.put("name", rider2.getName());
        rider2Info.put("numberOrders", 0);
        rider2Info.put("average", 0);

        Map<String, Object> response = new HashMap<>();
        response.put("riders", Arrays.asList(rider1Info, rider2Info));
        response.put("currentPage", 0);
        response.put("totalItems", 2);
        response.put("totalPages", 1);

        when(managerService.getRidersInformation(0, 10)).thenReturn(response);

        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "example_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(2)))
                .andExpect(jsonPath("totalPages", is(1)))
                .andExpect(jsonPath("['riders'].size()", is(2)))
                .andExpect(jsonPath("['riders'][0].average", is(( 3.5))))
                .andExpect(jsonPath("['riders'][0].numberOrders", is((2))))
                .andExpect(jsonPath("['riders'][1].numberOrders", is(0)))
                .andExpect(jsonPath("['riders'][1].average", is(0)));

        verify(managerService, times(1)).getRidersInformation(0, 10);
    }

    @Test
    public void testGetAllRidersInformationPageWithoutResults_thenOk() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("riders", new ArrayList<>());
        response.put("currentPage", 0);
        response.put("totalItems", 0);
        response.put("totalPages", 0);

        when(managerService.getRidersInformation(0, 10)).thenReturn(response);

        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "example_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalItems", is(0)))
                .andExpect(jsonPath("totalPages", is(0)))
                .andExpect(jsonPath("['riders'].size()", is(0)));

        verify(managerService, times(1)).getRidersInformation(0, 10);
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
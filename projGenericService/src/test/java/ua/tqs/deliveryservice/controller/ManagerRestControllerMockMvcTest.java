package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.Test;
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
import ua.tqs.deliveryservice.model.Address;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.services.ManagerService;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ManagerRestController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class ManagerRestControllerMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    // --------------------------------------------
    // --      MANAGER: GET ALL RIDERS INFO      --
    // --------------------------------------------

    @Test
    public void testInvalidPageNo_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "bad_token")
                .param("pageNo", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(managerService, times(0)).getRidersInformation(any(), any());
    }

    @Test
    public void testInvalidPageSize_thenBadRequest() throws Exception {
        mvc.perform(get("/manager/riders/all")
                .header("Authorization", "Bearer " + "bad_token")
                .param("pageSize", String.valueOf(-1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(managerService, times(0)).getRidersInformation(any(), any());
    }

    @Test
    public void testGetRidersInformationEverythingValid_thenOk() throws Exception {
        Address addr1 = new Address("Rua ABC, n. 99", "4444-555", "Aveiro", "Portugal");
        Address addr2 = new Address("Rua ABC, n. 922", "4444-555", "Aveiro", "Portugal");

        Rider rider1 = new Rider("Rider 1", "goodpwd", "rider1@ua.pt");
        Rider rider2 = new Rider("Rider 2", "goodpwd", "rider2@ua.pt");

        Purchase purchase1 = new Purchase(addr1, rider1, new Store("Loja do Manel11", "A melhor loja.", "exToken1", addr1), "Adelaide");
        Purchase purchase2 = new Purchase(addr2, rider1,  new Store("Loja do Manel22", "A melhor loja.", "exToken2", addr2), "Rita");

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
    public void testGetRidersInformationPageWithoutResults_thenOk() throws Exception {
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

}

package ua.tqs.deliveryservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.deliveryservice.services.PurchaseService;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(RiderRestController.class)
class RiderRestControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PurchaseService purchaseService;

    /*
    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageNo_thenBadRequest() throws Exception {
        ResponseEntity<String> response = testRestTemplate.
                getForEntity(getBaseUrl() + "/requests?pageNo=" + -1, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRiderOrderHistoryWhenInvalidPageSize_thenBadRequest() throws Exception {
        ResponseEntity<String> response = testRestTemplate.
                getForEntity(getBaseUrl() + "/requests?pageSize=" + 0, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testGetRiderOrderHistory_thenStatus200() throws Exception {
        ResponseEntity<Request[]> response = testRestTemplate.
                getForEntity(getBaseUrl() + "/requests", Request[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        List<Request> found = Arrays.asList(response.getBody());

        assertThat(found.size(), equalTo(this.savedRequests.size()));

        for (int i = 0; i < this.savedRequests.size(); i++) {
            assertThat(found.get(i).getLatitude(), equalTo(this.savedRequests.get(i).getLatitude()));
            assertThat(found.get(i).getLongitude(), equalTo(this.savedRequests.get(i).getLongitude()));
            assertThat(found.get(i).getId(), equalTo(this.savedRequests.get(i).getId()));
        }

    }

    @Test
    public void testGetRiderOrderHistoryPageNoWithoutResults_thenNoResults() throws Exception {
        ResponseEntity<Request[]> response = testRestTemplate.
                getForEntity(getBaseUrl() + "/requests?pageNo=" + 1, Request[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        List<Request> found = Arrays.asList(response.getBody());

        assertThat(found.size(), equalTo(0));
    }

    @Test
    public void testGetRiderOrderHistoryPageNoAndLimitedPageSize_thenLimitedResults() throws Exception {
        ResponseEntity<Request[]> response = testRestTemplate.
                getForEntity(getBaseUrl() + "/requests?pageNo=" + 1 + "&pageSize=" + 2, Request[].class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        List<Request> found = Arrays.asList(response.getBody());

        assertThat(found.size(), equalTo(1));

        assertThat(found.get(0).getLatitude(), equalTo(this.savedRequests.get(2).getLatitude()));
        assertThat(found.get(0).getLongitude(), equalTo(this.savedRequests.get(2).getLongitude()));
        assertThat(found.get(0).getId(), equalTo(this.savedRequests.get(2).getId()));
    }

    //simular exceção


     */

}
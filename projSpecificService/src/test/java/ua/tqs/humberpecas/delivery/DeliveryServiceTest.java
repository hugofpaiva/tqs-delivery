package ua.tqs.humberpecas.delivery;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.Review;

import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


@RestClientTest(DeliveryServiceImpl.class)
public class DeliveryServiceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IDeliveryService deliveryService;

    private MockRestServiceServer server;

    private Review review;

    @BeforeEach
    public void setUp(){

        server = MockRestServiceServer.createServer(restTemplate);


        review = new Review(12, 4);
    }

    @Test
    @DisplayName("Add review to Rider")
    void whenValidReview_sendToDeliveryApp() {

    }

    @Test
    @DisplayName("Add review of invalid order throws ResourseNotFound")
    void whenInvalidOrder_thenThrowsStatusResourseNotFound(){

        this.server
                .expect(ExpectedCount.once(), requestTo("http://localhost:8081/store/order/12/review"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows( ResourceNotFoundException.class, () -> {
            deliveryService.reviewRider(review);
        } );

    }


    @Test
    @DisplayName("Add review of invalid order throws UnreachableServer")
    void whenCannotConnect_thenThrowsStatusUnreachableServer(){

        this.server
                .expect(ExpectedCount.once(), requestTo("http://localhost:8081/store/order/12/review"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows( UnreachableServiceException.class, () -> {
            deliveryService.reviewRider(review);
        } );

    }

}

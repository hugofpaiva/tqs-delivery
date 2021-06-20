package ua.tqs.deliveryservice.specific;


import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.UnreachableServiceException;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.services.PurchaseService;

import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(SpecificServiceImpl.class)
public class SpecificServiceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ISpecificService specificService;

    private MockRestServiceServer server;


    @BeforeEach
    void setUp(){
        server = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    @DisplayName("Update Status with connection error throws UnreachableServer")
    void whenCannotConnect_thenThrowsStatusUnreachableServer(){

        String url = "http://localhost:8080/delivery/updateStatus?serverOrderId=5";

        this.server
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows( UnreachableServiceException.class, () -> {
            specificService.updateOrderStatus(Status.PICKED_UP, url);
        } );

    }


    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST","NOT_FOUND", "FORBIDDEN", "UNAUTHORIZED"})
    @DisplayName("Update Status with invalid Data error throws InvalidValue")
    void whenSpecificInvalidData_thenThrowsStatusInvalidValue(HttpStatus status){

        String url = "http://localhost:8080/delivery/updateStatus?serverOrderId=5";

        this.server
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(status));

        assertThrows( InvalidValueException.class, () -> {
            specificService.updateOrderStatus(Status.PICKED_UP, url);
        } );

    }

}

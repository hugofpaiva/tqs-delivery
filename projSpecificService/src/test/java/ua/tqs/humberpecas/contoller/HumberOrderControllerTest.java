package ua.tqs.humberpecas.contoller;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.execption.ResourceNotFoundException;
import ua.tqs.humberpecas.model.PurchageStatus;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.service.HumberService;

import java.io.IOException;

import static org.mockito.Mockito.*;


@WebMvcTest(HumberController.class)
public class HumberOrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberService service;


    @BeforeEach
    void setUp() throws IOException {
        RestAssuredMockMvc.mockMvc(mvc);

    }

    @Test
    @DisplayName("Get Status of an invalid Order return HTTP status BAD Request ")
    void whenGetStatusInvalidOrder_thenReturnStatus400() throws ResourceNotFoundException {

        when(service.checkPurchageStatus(anyLong())).thenThrow(new ResourceNotFoundException("Invalid Order !"));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/shop/order?orderId=0")
                .then()
                .statusCode(400);

        verify(service, times(1)).checkPurchageStatus(0);
    }

    @Test
    @DisplayName("Get Order Status")
    void whenGetOrderStatus_thenReturnStatus() throws ResourceNotFoundException {

        when(service.checkPurchageStatus(anyLong())).thenReturn(PurchageStatus.PENDENT);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.TEXT)
                .when()
                .get("/shop/order?orderId=0")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo(PurchageStatus.PENDENT.getStatus()) );

        verify(service, times(1)).checkPurchageStatus(0);
    }



}

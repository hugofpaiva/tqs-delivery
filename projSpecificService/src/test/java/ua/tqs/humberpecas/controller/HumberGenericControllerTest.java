package ua.tqs.humberpecas.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.dto.ServerRiderDTO;
import ua.tqs.humberpecas.dto.ServerStatusDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.InvalidOperationException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.PurchaseStatus;
import ua.tqs.humberpecas.service.HumberGenericServer;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(value = HumberGenericController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
public class HumberGenericControllerTest {


    @MockBean
    private HumberGenericServer genericServer;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private ServerStatusDTO serverStatusDTO;
    private String genericToken;
    private ServerRiderDTO serverRiderDTO;


    @BeforeEach
    void setUp(){

        RestAssuredMockMvc.mockMvc(mvc);
        serverRiderDTO = new ServerRiderDTO("tone");
        serverStatusDTO = new ServerStatusDTO(PurchaseStatus.ACCEPTED);
        genericToken = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDcwOTYwNDMsImlhdCI6MTYyMzA5OTI0MywiU3ViamVjdCI6Ikh1bWJlclBlY2FzIn0.oEZD63J134yUxHl658oSDJrw32BZcYHQbveZw8koAgP-2_d-8aH2wgJYJMlGnKIugOiI8H9Aa4OjPMWMUl9BFw";
    }


    @Test
    @DisplayName("Update Status")
    void whenGenericSendUpdate_thenUpdateStatus(){

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverStatusDTO)
                .when()
                .put("/delivery/updateStatus?serverOrderId=1")
                .then()
                .statusCode(200);

        verify(genericServer, times(1)).updateOrderStatus(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Update Status wtihout generic token throws BadRequest")
    void whenUpdateWithoutToken_thenThrowsStatusBadRequest(){

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(serverStatusDTO)
                .when()
                .put("/delivery/updateStatus?serverOrderId=1")
                .then()
                .statusCode(400);

        verify(genericServer, times(0)).updateOrderStatus(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Update Status with invalid generic token throws Unauthorized")
    void whenUpdateWithInvalidToken_thenThrowsStatusUnauthorized(){

        doThrow(new InvalidLoginException("Invalid token")).when(genericServer).updateOrderStatus(anyLong(), anyString(), any());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverStatusDTO)
                .when()
                .put("/delivery/updateStatus?serverOrderId=1")
                .then()
                .statusCode(401);

        verify(genericServer, times(1)).updateOrderStatus(anyLong(), anyString(), any());

    }


    @Test
    @DisplayName("Update Status of invalid order throws ResourseNotFound")
    void whenUpdateWithInvalidOrder_thenThrowsStatusResourseNotFound(){

        doThrow(new ResourceNotFoundException("Invalid token")).when(genericServer).updateOrderStatus(anyLong(), anyString(), any());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverStatusDTO)
                .when()
                .put("/delivery/updateStatus?serverOrderId=1")
                .then()
                .statusCode(404);

        verify(genericServer, times(1)).updateOrderStatus(anyLong(), anyString(), any());

    }


    @Test
    @DisplayName("Set Rider wtihout generic token throws BadRequest")
    void whenSetRiderWithoutToken_thenThrowsStatusBadRequest(){

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(serverRiderDTO)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(400);

        verify(genericServer, times(0)).setRider(anyLong(), anyString(), anyString());

    }

    @Test
    @DisplayName("Set Rider of invalid order throws ResourseNotFound")
    void whenSetRiderWithInvalidOrder_thenThrowsStatusResourseNotFound(){

        doThrow(new ResourceNotFoundException("Invalid token")).when(genericServer).setRider(anyLong(), anyString(), any());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverRiderDTO)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(404);

        verify(genericServer, times(1)).setRider(anyLong(), anyString(), anyString());

    }


    @Test
    @DisplayName("Set Rider of invalid order throws BadRequest")
    void whenSetRiderAssignedOrder_thenThrowsStatusBadRequest(){

        doThrow(new InvalidOperationException("Order Already Assigned")).when(genericServer).setRider(anyLong(), anyString(), any());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverRiderDTO)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(400);

        verify(genericServer, times(1)).setRider(anyLong(), anyString(), anyString());

    }

    @Test
    @DisplayName("Set Rider of invalid genric token throws UNAUTHORIZED")
    void whenSetRiderInvalidToken_thenThrowsStatusUNAUTHORIZED(){

        doThrow(new InvalidLoginException("Invalid token")).when(genericServer).setRider(anyLong(), anyString(), any());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverRiderDTO)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(401);

        verify(genericServer, times(1)).setRider(anyLong(), anyString(), anyString());

    }

    @Test
    @DisplayName("Set Rider")
    void whenSetRider_thenReturnStatusOk(){

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(serverRiderDTO)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(200);

        verify(genericServer, times(1)).setRider(anyLong(), anyString(), anyString());

    }

    @Test
    @DisplayName("Set Rider with invalid input token throws BadRequest")
    void whenSetRiderInvalidInput_thenThrowsStatusBadRequest(){

        Map<String, String> request = new HashMap<>();
        request.put("x", "y");

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", genericToken)
                .body(request)
                .when()
                .put("/delivery/setRider?serverOrderId=1")
                .then()
                .statusCode(400);

        verify(genericServer, times(0)).setRider(anyLong(), anyString(), anyString());

    }
}

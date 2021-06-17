package ua.tqs.humberpecas.controller;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.service.HumberAddressService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberAddressController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberAddressControllerMockMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HumberAddressService service;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    private Address  address;
    private AddressDTO addressDTO;
    private Person person;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mvc);
        person = new Person("Fernando", "12345678","fernando@ua.pt");
        address  = new Address("Aveiro", "3730-123","Aveiro","Portugal", person);
        addressDTO = new AddressDTO("Aveiro", "3730-123","Aveiro","Portugal");
    }

    // -------------------------------------
    // --       ADD ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Add new Address")
    void whenAddAdressValidUser_thenReturnStatusOk() throws ResourceNotFoundException, InvalidLoginException {
        when(service.addNewAddress("Bearer token", addressDTO)).thenReturn(address);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer token")
                .body(addressDTO)
                .when()
                .post("/address/add")
                .then()
                .body(is(equalTo("{\"id\":0,\"address\":\"Aveiro\",\"postalCode\":\"3730-123\",\"city\":\"Aveiro\",\"country\":\"Portugal\"}")))
                .statusCode(200);

        verify(service, times(1)).addNewAddress("Bearer token", addressDTO);
    }

    @Test
    @DisplayName("Add Address of Invalid returns HTTP Not Found")
    void whenAddAddressInvalidUser_thenReturnsStatus404() throws ResourceNotFoundException, InvalidLoginException {
        when(service.addNewAddress("Bearer bad_token", addressDTO)).thenThrow(new InvalidLoginException("Invalid User"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer bad_token")
                .body(addressDTO)
                .when()
                .post("/address/add")
                .then()
                .statusCode(401);

        verify(service, times(1)).addNewAddress("Bearer bad_token", addressDTO);
    }

    // -------------------------------------
    // --       GET ADDRESS TESTS         --
    // -------------------------------------

    @Test
    @DisplayName("Get User address")
    void whenGetAddressesValidUser_thenReturnAddress() throws ResourceNotFoundException, InvalidLoginException {
        Address address2 = new Address("Coimbra", "3730-125","Coimbra","Portugal", person);
        List<Address> addresses = Arrays.asList(address, address2);

        when(service.getUserAddress(anyString())).thenReturn(addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer token")
                .when()
                .get("/address/getAll?userId=1")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(2))
                .body("[0].address", equalTo("Aveiro"))
                .body("[1].address", equalTo("Coimbra"));

        verify(service, times(1)).getUserAddress(anyString());
    }


    @Test
    @DisplayName("Get Addresses of Invalid User returns HTTP Status Not Found")
    void whenGetAddressesInvalidUser_thenReturnStatus404() throws InvalidLoginException {
        when(service.getUserAddress(anyString())).thenThrow(new InvalidLoginException("Invalid user token"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer token")
                .when()
                .get("/address/getAll?userId=1")
                .then()
                .statusCode(401);

        verify(service, times(1)).getUserAddress(anyString());
    }

    // -------------------------------------
    // --       DELETE ADDRESS TESTS      --
    // -------------------------------------

    @Test
    @DisplayName("Delete User Address")
    void whenDeleteValidAddress_thenReturnStatusOk() throws ResourceNotFoundException, InvalidLoginException {
        doNothing().when(service).delAddress("Bearer token", address.getId());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer token")
                .param("addressId", address.getId())
                .body(addressDTO)
                .when()
                .delete("/address/del")
                .then()
                .statusCode(200);

        verify(service, times(1)).delAddress("Bearer token", address.getId());


    }

    @Test
    @DisplayName("Delete Address: InvalidId returns HTTP Not Found")
    void whenDeleteInvalidAddressId_thenReturnStatus404() throws ResourceNotFoundException, InvalidLoginException {
        doThrow(ResourceNotFoundException.class).when(service).delAddress("Bearer token", -1L);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer token")
                .param("addressId", -1L)
                .body(addressDTO)
                .when()
                .delete("/address/del")
                .then()
                .statusCode(404);

        verify(service, times(1)).delAddress("Bearer token", -1L);
    }

    @Test
    @DisplayName("Delete Address: InvalidId returns UNAUTHORIZED")
    void whenDeleteInvalidAddressId_thenReturnStatus401() throws ResourceNotFoundException, InvalidLoginException {
        doThrow(InvalidLoginException.class).when(service).delAddress("Bearer bad_token", address.getId());

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .header("authorization", "Bearer bad_token")
                .param("addressId",  address.getId())
                .body(addressDTO)
                .when()
                .delete("/address/del")
                .then()
                .statusCode(401);

        verify(service, times(1)).delAddress("Bearer bad_token", address.getId());
    }
}

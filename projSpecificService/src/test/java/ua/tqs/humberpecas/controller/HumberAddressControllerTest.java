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

import static org.mockito.Mockito.*;


@WebMvcTest(value = HumberAddressController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class HumberAddressControllerTest {

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

    @Test
    @DisplayName("Get User address")
    void whenGetAddressesValidUser_thenReturnAddress() throws ResourceNotFoundException, InvalidLoginException {


        Address address2 = new Address("Coimbra", "3730-125","Coimbra","Portugal", person);
        List<Address> addresses = Arrays.asList(address, address2);

        when(service.getUserAddress(anyString())).thenReturn(addresses);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/address/getAll?userId=1")
                .then()
                .statusCode(200)
                .body("$.size()", Matchers.equalTo(2))
                .body("[0].address", Matchers.equalTo("Aveiro"))
                .body("[1].address", Matchers.equalTo("Coimbra"));

        verify(service, times(1)).getUserAddress(anyString());


    }


    @Test
    @DisplayName("Get Addresses of Invalid User returns HTTP Status Not Found")
    void whenGetAddressesInalidUser_thenReturnStatus404() throws ResourceNotFoundException, InvalidLoginException {

        when(service.getUserAddress(anyString())).thenThrow(new ResourceNotFoundException("Invalid User!"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .when()
                .get("/address/getAll?userId=1")
                .then()
                .statusCode(404);

        verify(service, times(1)).getUserAddress(anyString());


    }


    @Test
    @DisplayName("Add new Address")
    void whenAddAdressValidUser_thenReturnStatusOk() throws ResourceNotFoundException {

        when(service.addNewAddress(addressDTO)).thenReturn(address);


        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(addressDTO)
                .when()
                .post("/address/add")
                .then()
                .statusCode(200);

        verify(service, times(1)).addNewAddress(addressDTO);


    }

    @Test
    @DisplayName("Add Address of Invalid returns HTTP Not Found")
    void whenAddAddressInvalidUser_thenReturnsStatus404() throws ResourceNotFoundException {

        when(service.addNewAddress(addressDTO)).thenThrow(new ResourceNotFoundException("Invalid Address"));

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(addressDTO)
                .when()
                .post("/address/add")
                .then()
                .statusCode(404);

        verify(service, times(1)).addNewAddress(addressDTO);

    }



    @Test
    @DisplayName("Delete User Address")
    void whenDeleteValidAddress_thenReturnStatusOk() throws ResourceNotFoundException {


        doNothing().when(service).delAddress(addressDTO);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(addressDTO)
                .when()
                .delete("/address/del")
                .then()
                .statusCode(200);

        verify(service, times(1)).delAddress(addressDTO);


    }

    @Test
    @DisplayName("Add Address of Invalid returns HTTP Not Found")
    void whenDeleteInalidAddress_thenReturnStatus404() throws ResourceNotFoundException {

        doThrow(ResourceNotFoundException.class).when(service).delAddress(addressDTO);

        RestAssuredMockMvc.given()
                .contentType("application/json")
                .body(addressDTO)
                .when()
                .delete("/address/del")
                .then()
                .statusCode(404);

        verify(service, times(1)).delAddress(addressDTO);

    }



}

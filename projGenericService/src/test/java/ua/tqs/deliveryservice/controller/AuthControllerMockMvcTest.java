package ua.tqs.deliveryservice.controller;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.deliveryservice.configuration.JwtRequestFilter;
import ua.tqs.deliveryservice.configuration.WebSecurityConfig;
import ua.tqs.deliveryservice.exception.DuplicatedObjectException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.JwtResponse;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;
import ua.tqs.deliveryservice.services.RiderService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Disables Security
@WebMvcTest(value = AuthController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerMockMvcTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RiderService riderService;

    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    // ----------------------------------------------
    // --              register tests              --
    // ----------------------------------------------

    // 1. despoletar erros no controller e ver se o service não chega a ser chamado

    @Test
    public void testInvalidEmail_thenBadRequest() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("email", null);
        data.put("name", "A Nice Name");
        data.put("pwd", "And_A_str0ngPasswor2drd");

        mvc.perform(post("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isBadRequest());

        Mockito.verify(riderService, VerificationModeFactory.times(0)).save(
                new Rider("A Nice Name", "And_A_str0ngPasswor2drd", null)
        );
    }

    @Test
    public void testInvalidPwd_thenBadRequest() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("email", "example@tqs.ua");
        data.put("name", "A Nice Name");
        data.put("pwd", "weak");

        mvc.perform(post("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isBadRequest());

        Mockito.verify(riderService, VerificationModeFactory.times(0)).save(
                new Rider("A Nice Name", "weak", "example@tqs.ua")
        );
    }

    @Test
    public void testInvalidName_thenBadRequest() throws Exception {
        JSONObject data = new JSONObject();
        data.put("email", "example@tqs.ua");
        data.put("name", null);
        data.put("pwd", "strongggg");


        mvc.perform(post("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isBadRequest());

        Mockito.verify(riderService, VerificationModeFactory.times(0)).save(new Rider(null, "strongggg", "example@tqs.ua"));
    }

    @Test
    public void testEverythingValid_thenCreated() throws Exception {
        Rider rider = new Rider("A very nice name", "strongggg", "example@tqs.ua");

        JSONObject data = new JSONObject();
        data.put("email", "example@tqs.ua");
        data.put("name", "A very nice name");
        data.put("pwd", "strongggg");

        when(riderService.save(rider)).thenReturn(rider);

        mvc.perform(post("/register")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(String.valueOf(data)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("email", is("example@tqs.ua")))
                    .andExpect(jsonPath("name", is("A very nice name")));

        Mockito.verify(riderService, VerificationModeFactory.times(1)).save(rider);
    }

    // erros no service

    @Test
    public void testRegister_whenEmailAlreadyInUse_then409() throws Exception {
        Rider rider = new Rider("A very nice name", "strongggg", "example@tqs.ua");

        JSONObject data = new JSONObject();
        data.put("email", "example@tqs.ua");
        data.put("name", "A very nice name");
        data.put("pwd", "strongggg");

        when(riderService.save(rider)).thenThrow(DuplicatedObjectException.class);

        mvc.perform(post("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isConflict());

        Mockito.verify(riderService, VerificationModeFactory.times(1)).save(rider);
    }

    // ----------------------------------------------
    // --                login tests               --
    // ----------------------------------------------

    @Test
    public void testLoginWhenInvalidCredentials_thenUnauthorized() throws Exception {
        JSONObject data = new JSONObject();
        data.put("username", "email@asd.com");
        data.put("password", "aswdd");

        when(jwtUserDetailsService.newAuthenticationToken(any())).thenThrow(InvalidLoginException.class);

        mvc.perform(post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1)).newAuthenticationToken(any());
    }

    @Test
    public void testLoginWhenValidDataRider_thenAuthorized() throws Exception {
        JSONObject data = new JSONObject();
        data.put("username", "mail@example.com");
        data.put("password", "aRightPassword");

        when(jwtUserDetailsService.newAuthenticationToken(any())).thenReturn(new JwtResponse("new_token", new SimpleGrantedAuthority("Rider"), "João"));

        mvc.perform(post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token", is("new_token")))
                .andExpect(jsonPath("['type']['authority']", is("Rider")));

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1)).newAuthenticationToken(any());
    }

    @Test
    public void testLoginWhenValidDataManager_thenAuthorized() throws Exception {
        JSONObject data = new JSONObject();
        data.put("username", "mail@example.com");
        data.put("password", "aRightPassword");

        when(jwtUserDetailsService.newAuthenticationToken(any())).thenReturn(new JwtResponse("new_token", new SimpleGrantedAuthority("Manager"), "João"));

        mvc.perform(post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("['token']", is("new_token")))
                .andExpect(jsonPath("['type']['authority']", is("Manager")));

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1)).newAuthenticationToken(any());
    }
}

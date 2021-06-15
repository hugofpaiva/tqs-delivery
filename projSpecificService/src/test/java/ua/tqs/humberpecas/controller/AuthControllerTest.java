package ua.tqs.humberpecas.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import ua.tqs.humberpecas.configuration.JwtRequestFilter;
import ua.tqs.humberpecas.configuration.WebSecurityConfig;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.model.JwtResponse;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Disables Security
@WebMvcTest(value = AuthController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebSecurityConfig.class)})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

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
    public void testLoginWhenValidData_thenAuthorized() throws Exception {
        JSONObject data = new JSONObject();
        data.put("username", "mail@example.com");
        data.put("password", "aRightPassword");

        when(jwtUserDetailsService.newAuthenticationToken(any())).thenReturn(new JwtResponse("new_token", new SimpleGrantedAuthority("Person"), "João"));

        mvc.perform(post("/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(String.valueOf(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token", is("new_token")))
                .andExpect(jsonPath("['type']['authority']", is("Person")));

        Mockito.verify(jwtUserDetailsService, VerificationModeFactory.times(1)).newAuthenticationToken(any());
    }

}
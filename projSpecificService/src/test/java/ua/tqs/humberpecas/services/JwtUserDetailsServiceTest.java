package ua.tqs.humberpecas.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ua.tqs.humberpecas.configuration.JwtTokenUtil;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.model.Generic;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.JwtResponse;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.GenericRepository;
import ua.tqs.humberpecas.repository.PersonRepository;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {
    @Mock
    private PersonRepository personRepository;

    @Mock
    private GenericRepository genericRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private JwtUserDetailsService jwtUserDetailsService;

    @Test
    public void testGivenNoPersons_whenGetUserByUsername_thenThrow() {
        Mockito.when(personRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            jwtUserDetailsService.loadUserByUsername("asd@gmail.com");
        }, "User not found with email: asd@gmail.com");
        Mockito.verify(personRepository, VerificationModeFactory.times(1))
                .findByEmail(anyString());
    }

    @Test
    public void testGivenNoStore_whenGetUserByStore_thenThrow() {
        assertThrows(BadCredentialsException.class, () -> {
            jwtUserDetailsService.loadUserByGeneric(null);
        }, "Generic cannot be null to create User");
    }

    @Test
    public void testGivenPerson_whenGetUserByUsername_thenReturnUserDetails() {
        Person person = new Person();
        String email = "asd@gmail.com";
        String name = "asd";
        String pwd = "pw-super-cool";
        person.setEmail(email);
        person.setName(name);
        person.setPwd(pwd);
        Mockito.when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);
        assertEquals(userDetails.getUsername(), email);
        assertEquals(userDetails.getPassword(), pwd);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(person.getClass().getSimpleName())));

        Mockito.verify(personRepository, VerificationModeFactory.times(1))
                .findByEmail(anyString());
    }

    @Test
    public void testGivenStore_whenGetUserByStore_thenReturnUserDetails() {
        Generic generic = new Generic();
        String name = "Cool Generic";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        generic.setName(name);
        generic.setToken(token);

        UserDetails userDetails = jwtUserDetailsService.loadUserByGeneric(generic);
        assertEquals(userDetails.getUsername(), name);
        assertEquals(userDetails.getPassword(), token);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(generic.getClass().getSimpleName())));
    }

    @Test
    public void testGivenHeaderAuthorization_whenGetEmailFromToken_thenReturnEmail() {
        String headerAuthorization = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2FvQGVtYWlsLmNvbSIsImV4cCI6MTYyMjg2NzY5MywiaWF0IjoxNjIyODQ5NjkzfQ.2kkxTG5mX0UcPGDnqFHElRJ-ny3jfh_qCfy_0lLbwo-XKZoz3eOu_pGyPKEi9zjkNvJs0Lwf51hU3l-dYzfaE";
        String email = "joao@example.com";
        Mockito.when(jwtTokenUtil.getUsernameFromToken(headerAuthorization.substring(7))).thenReturn(email);

        String returnedEmail = jwtUserDetailsService.getEmailFromToken(headerAuthorization);
        assertEquals(returnedEmail, email);

        Mockito.verify(jwtTokenUtil, VerificationModeFactory.times(1))
                .getUsernameFromToken(headerAuthorization.substring(7));
    }

    @Test
    public void testGivenHeaderAuthorizationAndStore_whenGetStoreFromToken_thenReturnStore() {
        Generic generic = new Generic();
        String name = "Cool Generic";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        generic.setName(name);
        generic.setToken(token);
        Mockito.when(genericRepository.findByToken(token)).thenReturn(Optional.of(generic));

        Generic returnedGeneric = jwtUserDetailsService.getGenericFromToken("Bearer " + token);
        assertEquals(returnedGeneric, generic);

        Mockito.verify(genericRepository, VerificationModeFactory.times(1))
                .findByToken(token);
    }

    @Test
    public void testGivenOnlyHeaderAuthorization_whenGetStoreFromToken_thenReturnNull() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        Mockito.when(genericRepository.findByToken(anyString())).thenReturn(Optional.empty());

        Generic returnedGeneric = jwtUserDetailsService.getGenericFromToken(token);
        assertEquals(returnedGeneric, null);

        Mockito.verify(genericRepository, VerificationModeFactory.times(1))
                .findByToken(anyString());
    }

    @Test
    public void testJwtRequestWithInvalidCredentials_thenThrow() {
        Mockito.when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        JwtRequest request = new JwtRequest("mail@example.com", "pw");

        assertThrows(InvalidLoginException.class, () -> {
            jwtUserDetailsService.newAuthenticationToken(request);
        }, "INVALID CREDENTIALS");

        Mockito.verify(authenticationManager, VerificationModeFactory.times(1))
                .authenticate(any());
        Mockito.verify(personRepository, VerificationModeFactory.times(0))
                .findByEmail(anyString());
        Mockito.verify(jwtTokenUtil, VerificationModeFactory.times(0))
                .generateToken(any());
    }

    @Test
    public void testJwtRequestWithPersonInvalid_thenThrow() {
        Mockito.when(personRepository.findByEmail("mail@example.com")).thenReturn(Optional.empty());

        JwtRequest request = new JwtRequest("mail@example.com", "pw");

        assertThrows(InvalidLoginException.class, () -> {
            jwtUserDetailsService.newAuthenticationToken(request);
        }, "Person not found for this email: mail@example.com");

        Mockito.verify(authenticationManager, VerificationModeFactory.times(1))
                .authenticate(any());
        Mockito.verify(personRepository, VerificationModeFactory.times(1))
                .findByEmail(anyString());
        Mockito.verify(jwtTokenUtil, VerificationModeFactory.times(0))
                .generateToken(any());
    }

    @Test
    public void testJwtRequestValid_thenReturnJwtResponse() throws InvalidLoginException {
        Person person = new Person("João", "password123", "mail@example.com");
        Mockito.when(personRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(person));

        JwtRequest request = new JwtRequest("mail@example.com", "password123");

        JwtResponse response = jwtUserDetailsService.newAuthenticationToken(request);

        assertEquals(response.getName(), person.getName());
        assertEquals(response.getType().toString(), "Person");

        Mockito.verify(authenticationManager, VerificationModeFactory.times(1))
                .authenticate(any());
        Mockito.verify(personRepository, VerificationModeFactory.times(2))
                .findByEmail(anyString());
        Mockito.verify(jwtTokenUtil, VerificationModeFactory.times(1))
                .generateToken(any());
    }


}
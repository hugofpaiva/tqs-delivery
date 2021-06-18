package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ua.tqs.deliveryservice.configuration.JwtTokenUtil;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.*;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {
    @Mock
    private PersonRepository personRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private JwtUserDetailsService jwtUserDetailsService;

    @Test
    public void testGivenNoRiderOrManager_whenGetUserByUsername_thenThrow() {
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
            jwtUserDetailsService.loadUserByStore(null);
        }, "Store cannot be null to create User");
    }

    @Test
    public void testGivenStore_whenGetUserByStore_thenReturnUserDetails() {
        Store store = new Store();
        String name = "Cool Store";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        store.setName(name);
        store.setToken(token);

        UserDetails userDetails = jwtUserDetailsService.loadUserByStore(store);
        assertEquals(userDetails.getUsername(), name);
        assertEquals(userDetails.getPassword(), token);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(store.getClass().getSimpleName())));
    }

    @Test
    public void testGivenRider_whenGetUserByUsername_thenReturnUserDetails() {
        Rider rider = new Rider();
        String email = "asd@gmail.com";
        String name = "asd";
        String pwd = "pw-super-cool";
        rider.setEmail(email);
        rider.setName(name);
        rider.setPwd(pwd);
        Mockito.when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(rider));

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);
        assertEquals(userDetails.getUsername(), email);
        assertEquals(userDetails.getPassword(), pwd);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(rider.getClass().getSimpleName())));

        Mockito.verify(personRepository, VerificationModeFactory.times(1))
                .findByEmail(anyString());
    }

    @Test
    public void testGivenManager_whenGetUserByUsername_thenReturnUserDetails() {
        Manager manager = new Manager();
        String email = "asd@gmail.com";
        String name = "asd";
        String pwd = "pw-super-cool";
        manager.setEmail(email);
        manager.setName(name);
        manager.setPwd(pwd);
        Mockito.when(personRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(email);
        assertEquals(userDetails.getUsername(), email);
        assertEquals(userDetails.getPassword(), pwd);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(manager.getClass().getSimpleName())));

        Mockito.verify(personRepository, VerificationModeFactory.times(1))
                .findByEmail(anyString());
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
        Store store = new Store();
        String name = "Cool Store";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        store.setName(name);
        store.setToken(token);
        Mockito.when(storeRepository.findByToken(token)).thenReturn(Optional.of(store));

        Store returnedStore = jwtUserDetailsService.getStoreFromToken("Bearer " + token);
        assertEquals(returnedStore, store);

        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
                .findByToken(token);
    }

    @Test
    public void testGivenOnlyHeaderAuthorization_whenGetStoreFromToken_thenReturnNull() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE5MDY4OTU2OTksImlhdCI6MTYyMjg5ODk1OX0.dgYxgi4nRUUpyL_hcNvkjei2_TX9AAPoUFJo99U_SlTrpE5zH7bTTxubl8-_slIvYSlyvgc_IVHvqTxZTskSsA";
        Mockito.when(storeRepository.findByToken(anyString())).thenReturn(Optional.empty());

        Store returnedStore = jwtUserDetailsService.getStoreFromToken(token);
        assertEquals(returnedStore, null);

        Mockito.verify(storeRepository, VerificationModeFactory.times(1))
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
        Rider rider = new Rider("João", "password123", "mail@example.com");
        Mockito.when(personRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(rider));

        JwtRequest request = new JwtRequest("mail@example.com", "password123");

        JwtResponse response = jwtUserDetailsService.newAuthenticationToken(request);

        assertEquals(response.getName(), rider.getName());
        assertEquals(response.getType().toString(), "Rider");

        Mockito.verify(authenticationManager, VerificationModeFactory.times(1))
                .authenticate(any());
        Mockito.verify(personRepository, VerificationModeFactory.times(2))
                .findByEmail(anyString());
        Mockito.verify(jwtTokenUtil, VerificationModeFactory.times(1))
                .generateToken(any());
    }


}
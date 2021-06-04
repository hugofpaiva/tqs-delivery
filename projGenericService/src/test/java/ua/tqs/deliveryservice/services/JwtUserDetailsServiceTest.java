package ua.tqs.deliveryservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ua.tqs.deliveryservice.configuration.JwtTokenUtil;
import ua.tqs.deliveryservice.model.Manager;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.PersonRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {
    @Mock
    private PersonRepository personRepository;

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


}
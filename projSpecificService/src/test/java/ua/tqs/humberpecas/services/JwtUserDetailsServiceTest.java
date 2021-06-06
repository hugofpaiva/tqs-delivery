package ua.tqs.humberpecas.services;

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
import ua.tqs.humberpecas.configuration.JwtTokenUtil;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {
    @Mock
    private PersonRepository personRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

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
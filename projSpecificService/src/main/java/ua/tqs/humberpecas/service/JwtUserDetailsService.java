package ua.tqs.humberpecas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.configuration.JwtTokenUtil;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.JwtResponse;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person user = personRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getClass().getSimpleName()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPwd(),
                authorities);
    }

    public String getEmailFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);
        return jwtTokenUtil.getUsernameFromToken(jwtToken);
    }

    public JwtResponse newAuthenticationToken(JwtRequest authenticationRequest) throws InvalidLoginException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidLoginException("INVALID CREDENTIALS");
        }

        Person p = personRepository.findByEmail(authenticationRequest.getUsername()).orElseThrow(() -> new InvalidLoginException("Person not found for this email: " + authenticationRequest.getUsername()));

        final UserDetails userDetails = this.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return new JwtResponse(token, userDetails.getAuthorities().iterator().next(), p.getName());
    }
}

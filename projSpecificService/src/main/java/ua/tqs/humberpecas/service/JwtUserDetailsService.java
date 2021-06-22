package ua.tqs.humberpecas.service;

import lombok.extern.log4j.Log4j2;
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
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Generic;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.JwtResponse;
import ua.tqs.humberpecas.model.Person;
import ua.tqs.humberpecas.repository.GenericRepository;
import ua.tqs.humberpecas.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;


@Service
@Log4j2
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private GenericRepository genericRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person user = personRepository.findByEmail(username).orElseThrow(() -> {
            log.error("HUMBER JWT USER DETAILS SERVICE: No person with that email was found, when" +
                    "getting user by username");
            return new UsernameNotFoundException("User not found with email: " + username);
        });

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getClass().getSimpleName()));

        log.info("HUMBER JWT USER DETAILS SERVICE: Successfully retrieved user by username");
        return new org.springframework.security.core.userdetails.
                User(user.getEmail(), user.getPwd(), authorities);
    }

    public UserDetails loadUserByGeneric(Generic generic) throws BadCredentialsException {
        if (generic == null) {
            throw new BadCredentialsException("Generic cannot be null to create User");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(generic.getClass().getSimpleName()));
        return new org.springframework.security.core.userdetails.User(generic.getName(), generic.getToken(),
                authorities);
    }

    public Generic getGenericFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);
        Generic generic = null;
        try {
            generic = genericRepository.findByToken(jwtToken).orElseThrow(() -> new ResourceNotFoundException("Generic not found for this Token"));
        } catch (ResourceNotFoundException e1) {
            log.info("Unable to get Generic from JWT Token");
        }

        return generic;
    }

    public String getEmailFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);

        log.info("HUMBER JWT USER DETAILS SERVICE: Successfully retrieved email from token");
        return jwtTokenUtil.getUsernameFromToken(jwtToken);
    }

    public JwtResponse newAuthenticationToken(JwtRequest authenticationRequest) throws InvalidLoginException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.error("HUMBER JWT USER DETAILS SERVICE: Invalid credentials when getting new authentication token");
            throw new InvalidLoginException("INVALID CREDENTIALS");
        }

        Person p = personRepository.findByEmail(authenticationRequest.getUsername()).orElseThrow(() -> {
            log.error("HUMBER JWT USER DETAILS SERVICE: No person with that email was found, " +
                    "when getting new authentication token");
            return new InvalidLoginException("Person not found for this email: " + authenticationRequest.getUsername());
        });

        final UserDetails userDetails = this.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        log.info("HUMBER JWT USER DETAILS SERVICE: Successfully retrieved new authentication token");
        return new JwtResponse(token, userDetails.getAuthorities().iterator().next(), p.getName());
    }
}

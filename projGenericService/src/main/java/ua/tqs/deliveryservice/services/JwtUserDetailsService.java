package ua.tqs.deliveryservice.services;

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
import ua.tqs.deliveryservice.configuration.JwtTokenUtil;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.JwtRequest;
import ua.tqs.deliveryservice.model.JwtResponse;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class JwtUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person user = personRepository.findByEmail(username).orElseThrow(() -> {
            log.error("JWT USER DETAILS SERVICE: Invalid user email when loading user by username");
            return new UsernameNotFoundException("User not found with email: " + username);
        });

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getClass().getSimpleName()));

        log.info("JWT USER DETAILS SERVICE: Retrieved user by username successfully");
        return new org.springframework.security.core.userdetails.
                User(user.getEmail(), user.getPwd(), authorities);
    }

    public UserDetails loadUserByStore(Store store) throws BadCredentialsException {
        if (store == null) {
            log.error("JWT USER DETAILS SERVICE: Store can not be null to create user, when loading user by store");
            throw new BadCredentialsException("Store cannot be null to create User");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(store.getClass().getSimpleName()));

        log.info("JWT USER DETAILS SERVICE: Retrieved user by store successfully");
        return new org.springframework.security.core.userdetails.
                User(store.getName(), store.getToken(), authorities);
    }

    public Store getStoreFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);
        Store store = null;
        try {
            store = storeRepository.findByToken(jwtToken).orElseThrow(() ->
                new ResourceNotFoundException("Store not found for this Token")
            );
        } catch (ResourceNotFoundException e1) {
            log.error("JWT USER DETAILS SERVICE: Unable to get Store from JWT Token");
        }

        log.info("JWT USER DETAILS SERVICE: Successfully retrieved store from token");
        return store;
    }

    public String getEmailFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);

        log.info("JWT USER DETAILS SERVICE: Successfully retrieved email from token");
        return jwtTokenUtil.getUsernameFromToken(jwtToken);
    }

    public JwtResponse newAuthenticationToken(JwtRequest authenticationRequest) throws InvalidLoginException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.error("JWT USER DETAILS SERVICE: Invalid credentials, when getting new authentication token");
            throw new InvalidLoginException("INVALID CREDENTIALS");
        }

        Person p = personRepository.findByEmail(authenticationRequest.getUsername()).orElseThrow(() -> {
            log.error("JWT USER DETAILS SERVICE: Invalid person email, when getting new authentication token");
            return new InvalidLoginException("Person not found for this email: " + authenticationRequest.getUsername());
        });

        final UserDetails userDetails = this.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        log.info("JWT USER DETAILS SERVICE: Successfully retrieved new authentication token");
        return new JwtResponse(token, userDetails.getAuthorities().iterator().next(), p.getName());
    }
}

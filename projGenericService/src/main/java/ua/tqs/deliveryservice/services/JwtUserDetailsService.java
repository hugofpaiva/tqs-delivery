package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.configuration.JwtTokenUtil;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Store;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StoreRepository storeRepository;

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

    public UserDetails loadUserByStore(Store store) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(store.getClass().getSimpleName()));
        return new org.springframework.security.core.userdetails.User(store.getName(), store.getToken(),
                authorities);
    }

    public Store getStoreFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);
        Store store = null;
        try {
            store = storeRepository.findByToken(jwtToken).orElseThrow(() -> new ResourceNotFoundException("Store not found for this Token"));
        } catch (ResourceNotFoundException e1) {
            logger.info("Unable to get Store from JWT Token");
        }

        return store;
    }

    public String getEmailFromToken(String headerAuthorization) {
        String jwtToken = headerAuthorization.substring(7);
        return jwtTokenUtil.getUsernameFromToken(jwtToken);
    }
}

package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.configuration.JwtTokenUtil;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.JwtRequest;
import ua.tqs.deliveryservice.model.JwtResponse;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;
import ua.tqs.deliveryservice.services.RiderService;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private RiderService riderService;

    @PostMapping("/register")
    public ResponseEntity<Rider> registerARider(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String pwd = payload.get("pwd");
        String name = payload.get("name");

        if (pwd.length() < 8 || email == null || name == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Rider data = riderService.save(new Rider(name, pwd, email));
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws InvalidLoginException {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidLoginException("INVALID CREDENTIALS");
        }

        Person p = personRepository.findByEmail(authenticationRequest.getUsername()).orElseThrow(() -> new InvalidLoginException("Person not found for this email: " + authenticationRequest.getUsername()));

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token, userDetails.getAuthorities().iterator().next(), p.getName()));
    }
}


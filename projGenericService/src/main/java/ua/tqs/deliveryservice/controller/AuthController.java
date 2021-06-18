package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.exception.DuplicatedObjectException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.JwtRequest;
import ua.tqs.deliveryservice.model.JwtResponse;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;
import ua.tqs.deliveryservice.services.RiderService;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private RiderService riderService;

    @PostMapping("/register")
    public ResponseEntity<Rider> registerARider(@RequestBody Map<String, String> payload) throws DuplicatedObjectException {
        String email = payload.get("email");
        String pwd = payload.get("pwd");
        String name = payload.get("name");

        if (pwd.length() < 8 || email == null || name == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Rider data = riderService.save(new Rider(name, pwd, email));
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws InvalidLoginException {

        JwtResponse response = jwtUserDetailsService.newAuthenticationToken(authenticationRequest);

        return ResponseEntity.ok(response);
    }
}


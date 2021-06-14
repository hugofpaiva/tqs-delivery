package ua.tqs.humberpecas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.model.JwtRequest;
import ua.tqs.humberpecas.model.JwtResponse;
import ua.tqs.humberpecas.service.JwtUserDetailsService;

@RestController
public class AuthController {

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws InvalidLoginException {

        JwtResponse response = userDetailsService.newAuthenticationToken(authenticationRequest);

        return ResponseEntity.ok(response);
    }
}


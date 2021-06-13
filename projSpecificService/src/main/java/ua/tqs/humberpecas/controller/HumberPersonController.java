package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.humberpecas.dto.PersonDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.service.HumberPersonService;

import javax.validation.Valid;

@RestController
@RequestMapping("/person")
public class HumberPersonController {

    @Autowired
    private HumberPersonService service;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@Valid @RequestBody PersonDTO person ) throws ResourceNotFoundException, DuplicatedObjectException {

        service.register(person);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }



}

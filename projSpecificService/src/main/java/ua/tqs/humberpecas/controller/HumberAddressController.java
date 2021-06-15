package ua.tqs.humberpecas.controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.service.HumberAddressService;

import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/address")
public class HumberAddressController {

    @Autowired
    private HumberAddressService service;
    
    @PutMapping("/update")
    public ResponseEntity<HttpStatus> updateUserAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        log.debug("Update User address");
        service.updateAddress(address);

        log.info("User address updated with success");
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Address>> getUserAddresses(@RequestHeader("authorization") String token) throws InvalidLoginException {

        log.debug("Get User addresses");
        List<Address> userAddresses = service.getUserAddress(token.substring(7));


        log.info("Return User addresses  with success");
        return ResponseEntity.ok().body(userAddresses);

    }

//    @GetMapping("/getDetails")
//    public ResponseEntity<Address> getAddressDetails(@RequestParam long userId, @RequestParam long addressId){
//
//        Address address =
//    }

    @PostMapping("/add")
    public ResponseEntity<HttpStatus> addNewAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        log.debug("Add new User addresses");
        service.addNewAddress(address);


        log.info("Add new address with success");
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping("/del")
    public ResponseEntity<HttpStatus> delAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        log.debug("Delete address");
        service.delAddress(address);

        log.info("Address deleted with success");
        return new ResponseEntity<>(HttpStatus.OK);

    }


}

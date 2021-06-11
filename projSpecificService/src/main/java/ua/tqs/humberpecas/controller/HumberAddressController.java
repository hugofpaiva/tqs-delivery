package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.AddressDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.services.HumberAddressService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/address")
public class HumberAddressController {

    @Autowired
    private HumberAddressService service;

    // TODO: Assumir que o id é o mesmo (mantem-se) e apenas se alteram os dados
    @PutMapping("/update")
    public ResponseEntity<HttpStatus> updateUserAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        service.updateAddress(address);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Address>> getUserAddresses(@RequestParam long userId) throws ResourceNotFoundException {

        List<Address> userAddresses = service.getUserAddress(userId);
        return ResponseEntity.ok().body(userAddresses);

    }

//    @GetMapping("/getDetails")
//    public ResponseEntity<Address> getAddressDetails(@RequestParam long userId, @RequestParam long addressId){
//
//        Address address =
//    }

    @PostMapping("/add")
    public ResponseEntity<HttpStatus> addNewAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        service.addNewAddress(address);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping("/del")
    public ResponseEntity<HttpStatus> delAddress(@Valid @RequestBody AddressDTO address) throws ResourceNotFoundException {

        service.delAddress(address);

        return new ResponseEntity<>(HttpStatus.OK);

    }


}

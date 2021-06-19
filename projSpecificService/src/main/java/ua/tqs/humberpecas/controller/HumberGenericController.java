package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.ServerRiderDTO;
import ua.tqs.humberpecas.dto.ServerStatusDTO;
import ua.tqs.humberpecas.exception.DuplicatedObjectException;
import ua.tqs.humberpecas.service.HumberGenericServer;

import javax.validation.Valid;

@RestController
@RequestMapping("/delivery")
public class HumberGenericController {

    @Autowired
    private HumberGenericServer genericServer;


    @PatchMapping("/updateStatus")
    public ResponseEntity<HttpStatus> register(@RequestParam Long serverOrderId, @RequestHeader("authorization") String token, @Valid @RequestBody ServerStatusDTO status) throws DuplicatedObjectException {

        genericServer.updateOrderStatus(serverOrderId, token.substring(7), status.getOrderStatus());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @PatchMapping("/setRider")
    public ResponseEntity<HttpStatus> setRider(@RequestHeader("authorization") String token, @RequestParam Long serverOrderId, @Valid @RequestBody ServerRiderDTO serverRiderDTO){

        genericServer.setRider(serverOrderId, token, serverRiderDTO.getRider());
        return new ResponseEntity<>(HttpStatus.OK);

    }



}

package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.service.HumberPurchaseService;

import java.util.List;

@RestController
@RequestMapping("/purchase")
public class HumberPurchaseController {

    @Autowired
    private HumberPurchaseService service;

    @PostMapping("/new")
    public ResponseEntity<HttpStatus> newOrder(@RequestBody PurchaseDTO order, @RequestHeader("authorization") String token) throws ResourceNotFoundException{

            service.newPurchase(order, token);
            return new ResponseEntity<>(HttpStatus.OK);

    }


    // TODO: enviar toda a informação
    @GetMapping("/status")
    public ResponseEntity<String> getOrderStatus(@RequestParam long orderId) throws ResourceNotFoundException {

        var status = service.checkPurchaseStatus(orderId);
        return ResponseEntity.ok(status.getStatus());

    }

    // TODO: // userId -> token
    @GetMapping("/getAll")
    public ResponseEntity<List<Purchase>> getUserPurchases(@RequestHeader("authorization") String token) throws ResourceNotFoundException {

            List<Purchase> purchaseList = service.getUserPurchases(token);
            return ResponseEntity.ok().body(purchaseList);

    }

}

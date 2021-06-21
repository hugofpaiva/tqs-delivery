package ua.tqs.humberpecas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.dto.PurchaseDTO;
import ua.tqs.humberpecas.exception.InvalidLoginException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.service.HumberPurchaseService;

import java.util.Map;

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

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getUserPurchases(@RequestHeader("authorization") String token,
                                                           @RequestParam(defaultValue = "0") int pageNo,
                                                           @RequestParam(defaultValue = "5") int pageSize) throws InvalidLoginException {
        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = service.getUserPurchases(pageNo, pageSize, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

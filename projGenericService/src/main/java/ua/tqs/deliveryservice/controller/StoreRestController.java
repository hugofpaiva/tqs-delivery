package ua.tqs.deliveryservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreRestController {

    @PostMapping("/purchase")
    public ResponseEntity<Object> receivePurchase(@RequestBody String jsonData) {
        System.out.println("!! " + jsonData + " !!");
        return null;
    }
}

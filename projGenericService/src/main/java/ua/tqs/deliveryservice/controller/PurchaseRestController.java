package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.services.PurchaseService;

import java.util.Map;

@RestController
@RequestMapping("/store")
public class PurchaseRestController {
    @Autowired
    private PurchaseService purchaseService;


    @PatchMapping("/order/{order_id}/review")
    // <?> explanation: https://www.baeldung.com/http-put-patch-difference-spring, https://www.ti-enxame.com/pt/java/metodo-spring-mvc-patch-atualizacoes-parciais/1041054404/
    public ResponseEntity<?> addReviewToRider(@PathVariable Long order_id, @RequestBody Map<String, Long> payload, @RequestHeader Map<String, String> headers) throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {
        String token = headers.get("authorization").substring(7);
        Long review = payload.get("review");

        if (order_id == null || review == null) throw new InvalidValueException("Necessary variables were not passed.");
        if (review > 5 || review < 0) throw new InvalidValueException("Invalid review values were passed.");

        purchaseService.reviewRiderFromSpecificOrder(token, order_id, review.intValue());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

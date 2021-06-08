package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.services.PurchaseService;

import java.util.Map;

@RestController
@RequestMapping("/store")
public class PurchaseRestController {
    @Autowired
    private PurchaseService purchaseService;


    @PatchMapping("/order/{order_id}/review")
    public ResponseEntity<Map<String, Long>> addReviewToRider(@PathVariable Long order_id, @RequestBody Map<String, Long> payload, @RequestHeader Map<String, String> headers) throws InvalidValueException, InvalidLoginException {
        String token = headers.get("authorization").substring(7);
        Long review = payload.get("review");
        if (order_id == null || review == null) throw new InvalidValueException("Necessary variables were not passed.");;
        if (review > 5 || review < 0) throw new InvalidValueException("Invalid review values were passed.");


        try {
            return purchaseService.reviewRiderFromSpecificOrder(token, order_id, review);
        } catch (InvalidLoginException e) { throw new InvalidLoginException("Unauthorized store."); }
    }
}

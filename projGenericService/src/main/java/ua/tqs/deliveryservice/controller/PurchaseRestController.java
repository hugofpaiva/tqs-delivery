package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.services.PurchaseService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/store")
public class PurchaseRestController {
    @Autowired
    private PurchaseService purchaseService;

    @PutMapping("/order/{order_id}/review")
    public ResponseEntity<Object> addReviewToRider(@PathVariable Long order_id, @RequestBody Map<String, Long> payload, @RequestHeader Map<String, String> headers)
            throws InvalidValueException, InvalidLoginException, ResourceNotFoundException {

        String token = headers.get("authorization").substring(7);
        Long review = payload.get("review");


        if (order_id == null || review == null || review > 5 || review < 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);


        purchaseService.reviewRiderFromSpecificOrder(token, order_id, review.intValue());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/order")
    public ResponseEntity<Object> receivePurchase(HttpServletRequest request, @RequestBody Map<String,  Object> data) throws InvalidValueException, InvalidLoginException {
        String token = request.getHeader("Authorization");
        Purchase newPurchase = purchaseService.receiveNewOrder(token, data);
        Map<String, Object> resp = new TreeMap<>();
        resp.put("orderId", newPurchase.getId());
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}

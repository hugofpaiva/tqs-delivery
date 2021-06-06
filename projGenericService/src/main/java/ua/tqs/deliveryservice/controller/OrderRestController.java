package ua.tqs.deliveryservice.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/store")
public class OrderRestController {

    @Autowired
    private PurchaseRepository purchaseRep;

    @PatchMapping("/order/{order_id}/status")
    public ResponseEntity<HttpStatus> addReviewToRider(@PathVariable long order_id, @RequestBody Map<String, String> payload) throws InvalidLoginException,  {
        // todo: check if the authenticated rider is the 'correct'
        // (needs security implemented)

        // invalidValueException BAD_REQUEST: se for negativo ou se for um float ou string
        // se nao vier, bad request apenas
        // se o cliente que esta a fazer o pedido e o que esta associado aquela order -> login invalido ou bad requesgt
        //
        Optional<Purchase> pur = purchaseRep.findById(order);
        if (pur.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = pur.get();

        if (review_value >= 0 && review_value <= 5) {
            purchase.setRiderReview(review_value);
            purchaseRep.save(purchase);

            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}

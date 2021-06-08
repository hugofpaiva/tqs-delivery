package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.StoreRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/store")
public class PurchaseRestController {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;
    private Long review;

    @PatchMapping("/order/{order_id}/review")
    public Object addReviewToRider(@PathVariable Long order_id, @RequestBody Map<String, String> payload, @RequestHeader Map<String, String> headers) throws InvalidValueException, InvalidLoginException {
        String token = headers.get("authorization").substring(7);
        if (storeRepository.findByToken(token).isEmpty()) throw new InvalidLoginException("Unauthorized store.");
        if (order_id == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<Purchase> requested_purchase = purchaseRepository.findById(order_id);
        if (requested_purchase.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = requested_purchase.get();
        if (purchase.getRiderReview() != null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        long store_id_of_where_purchase_was_supposedly_made = purchase.getStore().getId();
        long store_id_associated_to_token_passed = storeRepository.findByToken(token).get().getId();

        if (store_id_of_where_purchase_was_supposedly_made != store_id_associated_to_token_passed) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        try {
            review = Long.parseLong(payload.get("review"));
        } catch (NumberFormatException e) { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }
        if (review < 0 || review > 5) throw new InvalidValueException("Review value cannot be under 0 nor over 5.");
        purchase.setRiderReview(review.intValue());
        purchaseRepository.saveAndFlush(purchase);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

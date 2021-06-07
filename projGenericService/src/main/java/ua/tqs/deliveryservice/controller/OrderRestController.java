package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/store")
public class OrderRestController {

    @Autowired
    private PurchaseRepository purchaseRepository;
    private Long review;

    @PatchMapping("/order/{order_id}/review")
    public Object addReviewToRider(@PathVariable Long order_id, @RequestBody Map<String, String> payload) throws InvalidValueException {
        String token = payload.get("token");
        // todo: check if the authenticated rider is the 'correct'
        // (needs security implemented)

        // com a auth da loja sei qual a loja que está a fazer o pedido, ptt tenho de verificar se a order que está a ser pedida pertence a essa loja -> UNAUTHORIZED
        // isto vai implicar alterar os testes

        if (order_id == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Optional<Purchase> requested_purchase = purchaseRepository.findById(order_id);
        if (requested_purchase.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = requested_purchase.get();
        if (purchase.getRiderReview() != null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        try {
            review = Long.parseLong(payload.get("review"));
        } catch (NumberFormatException e) { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }
        if (review < 0 || review > 5) throw new InvalidValueException("Review value cannot be under 0 nor over 5.");
        purchase.setRiderReview(review.intValue());
        purchaseRepository.save(purchase);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

package ua.tqs.deliveryservice.controller;

import com.fasterxml.jackson.databind.ext.CoreXMLDeserializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private RiderRepository riderRep;

    @Autowired
    private PurchaseRepository purchaseRep;

    @PutMapping("order/{order_id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatusAuto(@PathVariable long order_id) {
        // get purchase if exists
        Optional<Purchase> pur = purchaseRep.findById(order_id);
        if (pur.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = pur.get();

        // todo: check if the authenticated rider is the 'correct'
        // (needs security implemented)

        // gets 'next' status and updates
        Status next = Status.getNext(purchase.getStatus());
        System.out.println(next);

        if (next == Status.PENDENT) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        purchase.setStatus(next);
        purchaseRep.save(purchase);

        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", order_id);
        ret.put("status", next);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("/review")
    public ResponseEntity<HttpStatus> addReviewToRider(@RequestParam long order, @RequestParam int review_value) {
        // todo: check if the authenticated rider is the 'correct'
        // (needs security implemented)

        // se a order ja tiver uma review -> bad request
        // com a auth da loja sei qual a loja que está a fazer o pedido, ptt tenho de verificar se a order que está a ser pedida pertence a essa loja -> UNAUTHORIZED
        // criar novas exceções, throws e throw new ...
        // isto vai implicar alterar os testes

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

package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @PatchMapping("order/{order_id}/status")
    public ResponseEntity<Map<String,Object>> updateOrderStatusAuto(@PathVariable long order_id) throws InvalidValueException {
        // get purchase if exists
        Optional<Purchase> requested_purchase = purchaseRepository.findById(order_id);
        if (requested_purchase.isEmpty()) throw new InvalidValueException("Id not associated with a purchase.");
        Purchase purchase = requested_purchase.get();

        // todo: check if the authenticated rider is the 'correct'
        // (needs security implemented)

        // gets 'next' status and updates
        Status next = Status.getNext(purchase.getStatus());

        if (next == Status.PENDENT) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        purchase.setStatus(next);
        purchaseRepository.save(purchase);

        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", order_id);
        ret.put("status", next);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> registerARider(@RequestBody Map<String, String> payload) throws Exception {
        // {"name":"carolina","password":"abc","email":"delivery@tqs.com"} @ http://localhost:8080/rider/register
        Rider newRider = new Rider();
        if(payload.get("pwd").length() < 8) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (payload.get("email") == "") return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (payload.get("name") == "") return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        newRider.setPwd(payload.get("pwd"));
        newRider.setEmail(payload.get("email"));
        newRider.setName(payload.get("name"));

        riderRepository.save(newRider);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}

package ua.tqs.deliveryservice.controller;

import com.fasterxml.jackson.databind.ext.CoreXMLDeserializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private RiderRepository riderRep;

    @Autowired
    private PersonRepository personRep;

    @Autowired
    private PurchaseRepository purchaseRep;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @PutMapping("order/{order_id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatusAuto(HttpServletRequest request, @PathVariable long order_id) {
        String requestTokenHeader = request.getHeader("Authorization");
        String email = jwtUserDetailsService.getEmailFromToken(requestTokenHeader);

        // getting Person who is making the request
        Person p = personRep.findByEmail(email).orElse(null);
        if (p == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);


        // get purchase if exists
        Optional<Purchase> pur = purchaseRep.findById(order_id);
        if (pur.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Purchase purchase = pur.get();

        // check if the authenticated rider is the 'correct'
        if (!purchase.getRider().equals(p)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // gets 'next' status and updates
        Status next = Status.getNext(purchase.getStatus());

        if (next == Status.PENDENT) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        purchase.setStatus(next);
        purchaseRep.save(purchase);

        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", order_id);
        ret.put("status", next);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

}

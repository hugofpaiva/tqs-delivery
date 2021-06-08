package ua.tqs.deliveryservice.controller;

import com.fasterxml.jackson.databind.ext.CoreXMLDeserializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
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


    @GetMapping("order")
    public ResponseEntity<Map<String, Object>> getOrder(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");
        String email = jwtUserDetailsService.getEmailFromToken(requestTokenHeader);

        // getting Person who is making the request
        Person p = personRep.findByEmail(email).orElse(null);
        if (!(p instanceof Rider)) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        // verify if Rider has any purchase to deliver
        Purchase unfinished = purchaseRep.findTopByRiderAndStatusIsNot((Rider) p, Status.DELIVERED);
        if (unfinished != null) {
            HashMap<String, Object> ret = new HashMap<>();
            ret.put("data", "this rider still has an order to deliver");
            return new ResponseEntity<>(ret, HttpStatus.FORBIDDEN);
        }


        Purchase purch = purchaseRep.findTopByRiderIsNullOrderByDate();

        // exemplo:
        // data: {date=2021-06-08 00:56:33.252, orderId=9, clientMame=client22, store={address={country=Portugal, address=Rua Loja Loja, n. 23, city=Porto, postalCode=3212-333}, name=Loja do Manel, id=8}, clientAddress={country=Portugal, address=Rua ABC, n. 99, city=Aveiro, postalCode=4444-555}}

        HashMap<String, Object> ret = new HashMap<>();

        if (purch == null) {
            ret.put("data", "No more orders available");
            return new ResponseEntity<>(ret, HttpStatus.OK);
        }

        purch.setRider((Rider) p);
        purch.setStatus(Status.ACCEPTED);

        purchaseRep.save(purch);

        ret.put("data", purch.getMap());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

}

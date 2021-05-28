package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rider")
public class RiderRestController {
    // dizer q começa td com /rider/ (?)

    @Autowired
    private RiderRepository riderRep;

    @PutMapping("/status/{newStatus}")
    public ResponseEntity<HttpStatus> updateOrderStatus(@PathVariable String newStatus) {

        // TODO: get id of rider somehow
        long rider_id = 1;

        // gets and verifies if rider is in the DB (should always happen if authenticated and rider...)
        Optional<Rider> riderOptional = riderRep.findById(rider_id);
        if (riderOptional.isEmpty()) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Rider rider = riderOptional.get();

        // get the last purchase assigned to rider
        List<Purchase> purchases = rider.getPurchases();
        Purchase purchase = purchases.get(purchases.size() -1);

        // checks if 'newStatus' is valid and updates the statue of the last purchase/order
        Status status = Status.getEnumByString(newStatus);
        if (status == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        purchase.setStatus(status);

        return new ResponseEntity<>(HttpStatus.OK);
    }

}

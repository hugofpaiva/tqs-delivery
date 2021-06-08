package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PurchaseRepository;

import java.util.HashMap;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRep;

    public Purchase getAvailableOrderForRider() {
        Purchase purch = purchaseRep.findTopByRiderIsNullOrderByDate();
        return purch; // null if none
    }

    public Purchase acceptOrder(Rider r, Purchase p) {
        p.setRider(r);
        p.setStatus(Status.ACCEPTED);
        return purchaseRep.save(p);
    }

    public Purchase getCurrentRiderOrder(Rider r) {
        // verify if Rider has any purchase to deliver
        Purchase unfinished = purchaseRep.findTopByRiderAndStatusIsNot(r, Status.DELIVERED);
        return unfinished; // null if there's none
    }

}

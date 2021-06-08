package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.*;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private RiderRepository riderRep;

    @Autowired
    private PurchaseRepository purchaseRepository;

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

        if (next == Status.PENDENT) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        purchase.setStatus(next);
        purchaseRep.save(purchase);

        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", order_id);
        ret.put("status", next);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Purchase>> getRiderOrders(@RequestParam(defaultValue = "0") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize){
            if (pageNo < 0 || pageSize <= 0){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("date").descending());

            Page<Purchase> pagedResult = purchaseRepository.findAll(paging);

            List<Purchase> response = new ArrayList<>();

            if(pagedResult.hasContent()) {
                response = pagedResult.getContent();
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

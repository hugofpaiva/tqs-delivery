package ua.tqs.deliveryservice.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.InvalidValueException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.services.PurchaseService;
import ua.tqs.deliveryservice.services.RiderService;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.TreeMap;

@Log4j2
@RestController
@RequestMapping("/rider")
public class RiderRestController {
    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private RiderService riderService;

    @PutMapping("order/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatusAuto(HttpServletRequest request) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        String requestTokenHeader = request.getHeader("Authorization");

        Purchase purchase = purchaseService.updatePurchaseStatus(requestTokenHeader);
        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", purchase.getId());
        ret.put("status", purchase.getStatus());

        Long time = purchase.getDeliveryTime();
        if (time != null) {
            ret.put("delivery_time", time);
        }

        log.info("RiderRestController: Order status updated with success ");

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


    @GetMapping("order/new")
    public ResponseEntity<Map<String, Object>> getNewOrder(HttpServletRequest request,
                                                           @RequestParam(required = false, defaultValue = "") Double latitude,
                                                           @RequestParam(required = false, defaultValue = "") Double longitude
    ) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException, InvalidValueException {
        String requestTokenHeader = request.getHeader("Authorization");

        Purchase purch;
        if (latitude == null || longitude == null) purch = purchaseService.getNewPurchase(requestTokenHeader);
        else purch = purchaseService.getNewPurchaseLoc(requestTokenHeader, latitude, longitude);

        HashMap<String, Object> ret = new HashMap<>();
        ret.put("data", purch.getMap());

        log.info("RiderRestController: Order assigned to Rider");
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("order/current")
    public ResponseEntity<Map<String, Object>> getCurrentOrder(HttpServletRequest request) throws InvalidLoginException, ResourceNotFoundException {
        String requestTokenHeader = request.getHeader("Authorization");
        Purchase current = purchaseService.getCurrentPurchase(requestTokenHeader);

        Map<String, Object> ret = new TreeMap<>();
        ret.put("data", current.getMap());
        log.info("RiderRestController: Rider current Order");
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getRiderOrders(HttpServletRequest request,
    @RequestParam(defaultValue = "0") int pageNo,
    @RequestParam(defaultValue = "10") int pageSize) throws InvalidLoginException {
        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String requestTokenHeader = request.getHeader("Authorization");
        Map<String, Object> response = purchaseService.getLastOrderForRider(pageNo, pageSize, requestTokenHeader);
        log.info("RiderRestController: Rider Orders");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getRatingStatistics(HttpServletRequest request) throws InvalidLoginException {
        String requestTokenHeader = request.getHeader("Authorization");
        Map<String, Object> resp = riderService.getRatingStatistics(requestTokenHeader);
        log.info("RiderRestController: Review Statistics");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

}

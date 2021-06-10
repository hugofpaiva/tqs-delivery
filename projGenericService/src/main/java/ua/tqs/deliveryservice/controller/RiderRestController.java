package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.ForbiddenRequestException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.exception.ResourceNotFoundException;
import ua.tqs.deliveryservice.model.Person;
import ua.tqs.deliveryservice.model.Purchase;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.model.Status;
import ua.tqs.deliveryservice.repository.PersonRepository;
import ua.tqs.deliveryservice.repository.PurchaseRepository;
import ua.tqs.deliveryservice.repository.RiderRepository;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;
import ua.tqs.deliveryservice.services.PurchaseService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.services.PurchaseService;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    private PurchaseService purchaseService;

    @PatchMapping("order/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatusAuto(HttpServletRequest request) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        String requestTokenHeader = request.getHeader("Authorization");


        Purchase purchase = purchaseService.updatePurchaseStatus(requestTokenHeader);
        // return order id and status
        Map<String, Object> ret = new HashMap<>();
        ret.put("order_id", purchase.getId());
        ret.put("status", purchase.getStatus());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


    @GetMapping("order/new")
    public ResponseEntity<Map<String, Object>> getNewOrder(HttpServletRequest request) throws InvalidLoginException, ForbiddenRequestException, ResourceNotFoundException {
        String requestTokenHeader = request.getHeader("Authorization");


        Purchase purch = purchaseService.getNewPurchase(requestTokenHeader);
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("data", purch.getMap());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("order/current")
    public ResponseEntity<Map<String, Object>> getCurrentOrder(HttpServletRequest request) throws InvalidLoginException, ResourceNotFoundException {
        String requestTokenHeader = request.getHeader("Authorization");
        Purchase current = purchaseService.getCurrentPurchase(requestTokenHeader);

        Map<String, Object> ret = new TreeMap<>();
        ret.put("data", current.getMap());
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

            return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

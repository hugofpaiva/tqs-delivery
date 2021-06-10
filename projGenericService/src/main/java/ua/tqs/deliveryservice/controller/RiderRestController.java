package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.services.PurchaseService;

import javax.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/rider")
public class RiderRestController {

    @Autowired
    PurchaseService purchaseService;

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

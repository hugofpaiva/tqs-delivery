package ua.tqs.deliveryservice.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.services.StoreService;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/manager")
public class ManagerRestController {
    @Autowired
    private StoreService storeService;

    @GetMapping("/stores")
    public ResponseEntity<Map<String, Object>> getRiderOrders(HttpServletRequest request,
                                                              @RequestParam(defaultValue = "0") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize) throws InvalidLoginException {
        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String requestTokenHeader = request.getHeader("Authorization");

        Map<String, Object> response = storeService.getStores(pageNo, pageSize, requestTokenHeader);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(HttpServletRequest request) throws InvalidLoginException, InterruptedException {

        String requestTokenHeader = request.getHeader("Authorization");

        storeService.getStatistics(requestTokenHeader);

        return new ResponseEntity<>(null, HttpStatus.OK);
    }




}

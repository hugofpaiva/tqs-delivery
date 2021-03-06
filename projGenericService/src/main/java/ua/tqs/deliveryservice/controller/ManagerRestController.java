package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.services.ManagerService;
import ua.tqs.deliveryservice.services.PurchaseService;
import ua.tqs.deliveryservice.services.StoreService;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/manager")
public class ManagerRestController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private PurchaseService purchaseService;


    @GetMapping("riders/all")
    public ResponseEntity<Map<String, Object>> getAllRidersInfo(
        @RequestParam(defaultValue = "0") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize) {

        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = managerService.getRidersInformation(pageNo, pageSize);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/stores")
    public ResponseEntity<Map<String, Object>> getRiderOrders(@RequestParam(defaultValue = "0") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = storeService.getStores(pageNo, pageSize);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(HttpServletRequest request) {
        Map<String, Object> response = storeService.getStatistics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("riders/stats")
    public ResponseEntity<Map<String, Object>> getRidersStats() {
        Map<String, Object> response = managerService.getRidersStatistics();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("riders/top_delivered_cities")
    public ResponseEntity<Map<String, Object>> getTopDeliveredCities() {
        Map<String, Object> response = purchaseService.getTop5Cities();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

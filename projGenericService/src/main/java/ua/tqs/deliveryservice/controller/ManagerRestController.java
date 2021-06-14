package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.services.ManagerService;
import java.util.Map;

@RestController
@RequestMapping("/manager")
public class ManagerRestController {
    @Autowired
    private ManagerService managerService;


    @GetMapping("riders/all")
    public ResponseEntity<Map<String, Object>> getAllRidersInfo(
        @RequestParam(defaultValue = "0") int pageNo,
        @RequestParam(defaultValue = "10") int pageSize) throws InvalidLoginException {

        if (pageNo < 0 || pageSize <= 0){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> response = managerService.getRidersInformation(pageNo, pageSize);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.deliveryservice.model.Rider;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.services.PurchaseService;

import javax.servlet.http.HttpServletRequest;


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


    @PostMapping("/register")
    public ResponseEntity<HttpStatus> registerARider(@RequestBody Map<String, String> payload) throws Exception {
        // falta dividir este em serviço e mudar os testes, alterar a mail confirmation -> está mal
        Rider newRider = new Rider();
        if(payload.get("pwd").length() < 8) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (payload.get("email") == "") return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (payload.get("name") == "") return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        newRider.setPwd(payload.get("pwd"));
        newRider.setEmail(payload.get("email"));
        newRider.setName(payload.get("name"));

        // riderRepository.save(newRider);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}

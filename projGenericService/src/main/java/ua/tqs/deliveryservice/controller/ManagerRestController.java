package ua.tqs.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.deliveryservice.services.JwtUserDetailsService;
import ua.tqs.deliveryservice.services.ManagerService;
import ua.tqs.deliveryservice.services.RiderService;

@RestController
public class ManagerRestController {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private ManagerService managerService;

    // USAR HTTPSTATUS
    // USAR O SERVICE

}

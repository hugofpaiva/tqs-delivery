package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

@Service
public class RiderService {

    @Autowired
    RiderRepository riderRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public Rider save(Rider rider) {
        rider.setPwd(bcryptEncoder.encode(rider.getPwd()));
        riderRepository.saveAndFlush(rider);
        return rider;
    }
}

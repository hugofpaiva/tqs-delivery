package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

@Service
public class RiderService {
    @Autowired
    RiderRepository riderRepository;

    public Rider save(Rider rider) {

        riderRepository.saveAndFlush(rider);
        return rider;
    }
}

package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.Map;

import java.util.TreeMap;


@Service
public class RiderService {
    @Autowired
    RiderRepository riderRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Rider save(Rider rider) {
        rider.setPwd(bcryptEncoder.encode(rider.getPwd()));
        riderRepository.saveAndFlush(rider);
        return rider;
    }

    public Map<String, Object> getRatingStatistics(String riderToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(riderToken);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> new InvalidLoginException("There is no Rider associated with this token"));

        long nrReviews = rider.getTotalNumReviews();
        Double avg = nrReviews == 0 ? null : ((double) rider.getReviewsSum()) / nrReviews;


        Map<String, Object> resp = new TreeMap<>();
        resp.put("totalNumReviews", nrReviews);
        resp.put("avgReviews", avg);
        return resp;
    }
}

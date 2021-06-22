package ua.tqs.deliveryservice.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ua.tqs.deliveryservice.exception.DuplicatedObjectException;
import ua.tqs.deliveryservice.exception.InvalidLoginException;
import ua.tqs.deliveryservice.model.Rider;
import ua.tqs.deliveryservice.repository.RiderRepository;

import java.util.Map;

import java.util.TreeMap;


@Service
@Log4j2
public class RiderService {
    @Autowired
    RiderRepository riderRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public Rider save(Rider rider) throws DuplicatedObjectException {
        if (riderRepository.findByEmail(rider.getEmail()).isEmpty()) {
            rider.setPwd(bcryptEncoder.encode(rider.getPwd()));
            riderRepository.saveAndFlush(rider);

            log.info("RIDER SERVICE: Rider saved successfully");
            return rider;
        }

        log.error("RIDER SERVICE: Duplicated rider email, when saving rider");
        throw new DuplicatedObjectException("Rider with this email already exists.");

    }

    public Map<String, Object> getRatingStatistics(String riderToken) throws InvalidLoginException {
        String email = jwtUserDetailsService.getEmailFromToken(riderToken);
        Rider rider = riderRepository.findByEmail(email).orElseThrow(() -> {
            log.error("RIDER SERVICE: Invalid rider token when getting rating statistics");
            return new InvalidLoginException("There is no Rider associated with this token");
        });

        long nrReviews = rider.getTotalNumReviews();
        Double avg = nrReviews == 0 ? null : ((double) rider.getReviewsSum()) / nrReviews;


        Map<String, Object> resp = new TreeMap<>();
        resp.put("totalNumReviews", nrReviews);
        resp.put("avgReviews", avg);

        log.info("RIDER SERVICE: Retrieved rating statistics with success");
        return resp;
    }
}

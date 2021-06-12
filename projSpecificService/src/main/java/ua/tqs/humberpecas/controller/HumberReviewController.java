package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.services.HumberReviewService;

import javax.validation.Valid;

@RestController
@RequestMapping("/review")
public class HumberReviewController {

    @Autowired
    private HumberReviewService service;

    @PostMapping("/add")
    public ResponseEntity<HttpStatus> giveReview(@Valid @RequestBody Review review) throws ResourceNotFoundException {

        service.addReview(review);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

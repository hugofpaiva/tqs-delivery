package ua.tqs.humberpecas.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.tqs.humberpecas.exception.AccessNotAllowedException;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.exception.UnreachableServiceException;
import ua.tqs.humberpecas.model.Review;
import ua.tqs.humberpecas.service.HumberReviewService;

import javax.validation.Valid;

@RestController
@RequestMapping("/review")
public class HumberReviewController {

    @Autowired
    private HumberReviewService service;



    @PostMapping("/add")
    public ResponseEntity<HttpStatus> giveReview(@Valid @RequestBody Review review, @RequestHeader("authorization") String token)
            throws ResourceNotFoundException, UnreachableServiceException, AccessNotAllowedException {

        service.addReview(review, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

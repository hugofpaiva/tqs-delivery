package ua.tqs.deliveryservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ua.tqs.deliveryservice.repository.RiderRepository;

@Service
public class ManagerService {
    @Autowired
    private RiderRepository riderRepository;

    // TEM DE SER PAGEABLE
    // USAR EXCEPTIONS
    // USAR O RIDER REPOSITORY
    // rider -> purchase -> status (

}

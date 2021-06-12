package ua.tqs.humberpecas.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.repository.ProductRepository;

import java.util.List;

@Service
public class HumberProductService {

    @Autowired
    private ProductRepository repository;

    public List<Product> getCatolog(){

        return repository.findAll();

    }

    public Product getProductById(long productId) throws ResourceNotFoundException {

       return  repository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Invalid Product"));
    }


    public List<Product> getProductsByCategory(Category category) throws ResourceNotFoundException {

        return  repository.findByCategory(category).orElseThrow(() -> new ResourceNotFoundException("Invalid Product"));
    }


}

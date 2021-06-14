package ua.tqs.humberpecas.services;

import ua.tqs.humberpecas.service.HumberProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.tqs.humberpecas.exception.ResourceNotFoundException;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class HumberProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private HumberProductService service;

    private List<Product> catalog;

    @BeforeEach
    public void setUp() {

        catalog = Arrays.asList(
                new Product("Parafuso", 0.50, Category.SCREWS, "xpto",  "image_url"),
                new Product("Chave ingles", 5.00, Category.SCREWDRIVER, "xpto",  "image_url")
        );
    }


    @Test
    @DisplayName("Get All Products")
    void whenGetAll_thenReturnProducts(){

        Mockito.when(repository.findAll()).thenReturn(catalog);

        List<Product> productList = service.getCatolog();

        assertThat(productList, hasSize(2));
        assertThat(productList, hasItem(hasProperty("name", Matchers.equalTo("Parafuso"))));
        assertThat(productList, hasItem(hasProperty("name", Matchers.equalTo("Chave inglesa"))));
        verify(repository, times(1)).findAll();

    }

    @Test
    @DisplayName("Get Specific Product")
    void whenGetValidProductId_thenReturnProduct() throws ResourceNotFoundException {

        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(catalog.get(0)));

        Product p = service.getProductById(1L);

        assertThat(p.getName(), Matchers.equalTo("Parafuso"));

        verify(repository, times(1)).findById(1L);


    }

    @Test
    @DisplayName("Get Product with invalid Id throws ResourceNotFoundException")
    void whenGetInvalidProductId_thenThrowsResourceNotFound() throws ResourceNotFoundException {

        assertThrows( ResourceNotFoundException.class, () -> {
            service.getProductById(1L);
        } );

        verify(repository, times(1)).findById(1L);

    }


}

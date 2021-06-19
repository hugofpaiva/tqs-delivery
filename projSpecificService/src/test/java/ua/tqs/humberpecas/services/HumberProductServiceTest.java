package ua.tqs.humberpecas.services;

import org.springframework.data.domain.*;
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

import java.util.*;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class HumberProductServiceTest {
    Product parafuso = new Product("Parafuso", 0.50, Category.SCREWS, "xpto", "image_url");
    Product chave = new Product("Chave inglesa", 5.00, Category.SCREWDRIVER, "xpto", "image_url");

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private HumberProductService productService;

    private List<Product> catalog;

    @BeforeEach
    public void setUp() {
        catalog = Arrays.asList(parafuso, chave);
    }

    // -------------------------------------
    // --   GET FILTERED PRODUCTS TESTS   --
    // -------------------------------------

    @Test
    @DisplayName("Get Filtered Products: filter by name and category then return")
    void whenGetFilteredProducts_whenFilterByNameAndCategory_thenReturnProducts() {
        Pageable paging = PageRequest.of(0, 9, Sort.by("id").descending());
        Page<Product> result = new PageImpl<>(Arrays.asList(parafuso));

        Mockito.when(
                productRepository.findAllByCategoryAndNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (parafuso.getCategory(), parafuso.getName(), 0.0, 10000.0, paging)
        ).thenReturn(result);

        Map<String, Object> productList = productService.
                getProductsFiltered(0, 9, parafuso.getName(), 10000.0, 0.0, null, parafuso.getCategory());

        assertThat((List<Product>) productList.get("products"), hasSize(1));
        assertThat(productList.get("currentPage"), equalTo(0));
        assertThat(productList.get("totalItems"), equalTo(1L));
        assertThat(productList.get("totalPages"), equalTo(1));

        assertThat((List<Product>) productList.get("products"), hasItem(hasProperty("name",
                Matchers.equalTo(parafuso.getName()))));

        verify(productRepository, times(1)).
                findAllByCategoryAndNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (parafuso.getCategory(), parafuso.getName(), 0.0, 10000.0, paging);
    }

    @Test
    @DisplayName("Get Filtered Products: filter by name then return")
    void whenGetFilteredProducts_whenFilterByName_thenReturnProducts() {
        Pageable paging = PageRequest.of(0, 9, Sort.by("id").descending());
        Page<Product> result = new PageImpl<>(Arrays.asList(parafuso));

        Mockito.when(
                productRepository.findAllByNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (parafuso.getName(), 0.0, 10000.0, paging)
        ).thenReturn(result);

        Map<String, Object> productList = productService.
                getProductsFiltered(0, 9, parafuso.getName(), 10000.0, 0.0, null, null);

        assertThat((List<Product>) productList.get("products"), hasSize(1));
        assertThat(productList.get("currentPage"), equalTo(0));
        assertThat(productList.get("totalItems"), equalTo(1L));
        assertThat(productList.get("totalPages"), equalTo(1));

        assertThat((List<Product>) productList.get("products"), hasItem(hasProperty("name",
                Matchers.equalTo(parafuso.getName()))));

        verify(productRepository, times(1)).
                findAllByNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (parafuso.getName(), 0.0, 10000.0, paging);
    }

    @Test
    @DisplayName("Get Filtered Products: filter by category then return")
    void whenGetFilteredProducts_whenFilterByCategory_thenReturnProducts() {
        Pageable paging = PageRequest.of(0, 9, Sort.by("id").descending());
        Page<Product> result = new PageImpl<>(Arrays.asList(chave));

        Mockito.when(
                productRepository.findAllByCategoryAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (chave.getCategory(), 0.0, 10000.0, paging)
        ).thenReturn(result);

        Map<String, Object> productList = productService.
                getProductsFiltered(0, 9, null, 10000.0, 0.0, null, chave.getCategory());

        assertThat((List<Product>) productList.get("products"), hasSize(1));
        assertThat(productList.get("currentPage"), equalTo(0));
        assertThat(productList.get("totalItems"), equalTo(1L));
        assertThat(productList.get("totalPages"), equalTo(1));

        assertThat((List<Product>) productList.get("products"), hasItem(hasProperty("name",
                Matchers.equalTo(chave.getName()))));

        verify(productRepository, times(1)).
                findAllByCategoryAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual
                        (chave.getCategory(), 0.0, 10000.0, paging);
    }

    @Test
    @DisplayName("Get Filtered Products: no filters then return")
    void whenGetFilteredProducts_whenNoFilters_thenReturnProducts() {
        Pageable paging = PageRequest.of(0, 9, Sort.by("id").descending());
        Page<Product> result = new PageImpl<>(Arrays.asList(chave, parafuso));

        Mockito.when(productRepository.findAllByPriceIsGreaterThanEqualAndPriceIsLessThanEqual(0.0,10000.0,paging)).thenReturn(result);

        Map<String, Object> productList = productService.
                getProductsFiltered(0, 9, null, 10000.0, 0.0, null, null);

        assertThat((List<Product>) productList.get("products"), hasSize(2));
        assertThat(productList.get("currentPage"), equalTo(0));
        assertThat(productList.get("totalItems"), equalTo(2L));
        assertThat(productList.get("totalPages"), equalTo(1));

        assertThat((List<Product>) productList.get("products"), hasItem(hasProperty("name",
                Matchers.equalTo(chave.getName()))));

        assertThat((List<Product>) productList.get("products"), hasItem(hasProperty("name",
                Matchers.equalTo(parafuso.getName()))));

        verify(productRepository, times(1)).findAllByPriceIsGreaterThanEqualAndPriceIsLessThanEqual(0.0, 10000.0, paging);
    }

    // -------------------------------------
    // --      GET ALL PRODUCTS TESTS     --
    // -------------------------------------

    @Test
    @DisplayName("Get All Products")
    void whenGetAllProducts_thenReturnProducts() {
        Mockito.when(productRepository.findAll()).thenReturn(catalog);

        List<Product> productList = productService.getCatalog();

        assertThat(productList, hasSize(2));
        assertThat(productList, hasItem(hasProperty("name", Matchers.equalTo("Parafuso"))));
        assertThat(productList, hasItem(hasProperty("name", Matchers.equalTo("Chave inglesa"))));
        verify(productRepository, times(1)).findAll();
    }

    // -------------------------------------
    // --    GET SPECIFIC PRODUCT TESTS   --
    // -------------------------------------

    @Test
    @DisplayName("Get Specific Product")
    void whenGetValidProductId_thenReturnProduct() throws ResourceNotFoundException {

        Mockito.when(productRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(catalog.get(0)));

        Product p = productService.getProductById(1L);

        assertThat(p.getName(), Matchers.equalTo("Parafuso"));

        verify(productRepository, times(1)).findById(1L);


    }

    @Test
    @DisplayName("Get Product with invalid Id throws ResourceNotFoundException")
    void whenGetInvalidProductId_thenThrowsResourceNotFound() throws ResourceNotFoundException {

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(1L);
        });

        verify(productRepository, times(1)).findById(1L);

    }


}

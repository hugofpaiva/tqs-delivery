package ua.tqs.humberpecas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByCategoryAndNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(Category category, String name, Double priceMin, Double priceMax, Pageable pageable);
    Page<Product> findAllByNameContainingIgnoreCaseAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(String name, Double priceMin, Double priceMax, Pageable pageable);
    Page<Product> findAllByCategoryAndPriceIsGreaterThanEqualAndPriceIsLessThanEqual(Category category, Double priceMin, Double priceMax, Pageable pageable);
    Page<Product> findAllByPriceIsGreaterThanEqualAndPriceIsLessThanEqual(Double priceMin, Double priceMax, Pageable pageable);

}

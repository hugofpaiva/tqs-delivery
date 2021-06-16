package ua.tqs.humberpecas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByCategoryAndNameContainingAndPriceGreaterThanEqualAndPriceLessThanEqual(Category category, String name, Double priceMax, Double priceMin, Pageable pageable);
    Page<Product> findAllByNameContainingIgnoreCaseAndPriceGreaterThanEqualAndPriceLessThanEqual(String name, Double priceMax, Double priceMin, Pageable pageable);
    Page<Product> findAllByCategoryAndPriceGreaterThanEqualAndPriceLessThanEqual(Category category, Double priceMax, Double priceMin, Pageable pageable);


}

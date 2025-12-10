package com.milktea.app.repository; 
// Placeholder for ProductRepository interface 
// File: milktea-backend/src/main/java/com.milktea.app/repository/ProductRepository.java

import com.milktea.app.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> ,JpaSpecificationExecutor<ProductEntity>{
    List<ProductEntity> findByCategoryIdAndIsActiveTrue(Long categoryId);
    Page<ProductEntity> findByIsActiveTrue(Pageable pageable);
    List<ProductEntity> findByIsRecommendTrueAndIsActiveTrue();
    List<ProductEntity> findByIsHotTrueAndIsActiveTrueOrderByMonthlySalesDesc();
    List<ProductEntity> findByCategoryId(Long categoryId);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    @Query(value = "SELECT * FROM products WHERE is_active = true AND price BETWEEN :minPrice AND :maxPrice", nativeQuery = true)
    List<ProductEntity> findProductsByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM ProductEntity p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND p.isActive = true " +
            "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<ProductEntity> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
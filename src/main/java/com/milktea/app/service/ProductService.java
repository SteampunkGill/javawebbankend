// File: milktea-backend/src/main/java/com.milktea.app/service/ProductService.java
package com.milktea.app.service;

import com.milktea.app.dto.product.ProductDetailResDTO;
import com.milktea.app.dto.product.ProductFavoriteStatusResDTO; // New DTO
import com.milktea.app.dto.product.ProductListReqDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductListResDTO getProducts(ProductListReqDTO reqDTO, Pageable pageable); // Filter parameter in reqDTO
    ProductListResDTO getCategoryProducts(Long categoryId, ProductListReqDTO reqDTO, Pageable pageable); // New method
    ProductDetailResDTO getProductDetail(Long userId, Long productId);
    void addFavoriteProduct(Long userId, Long productId);
    void removeFavoriteProduct(Long userId, Long productId);
    // New: 2.3.4 获取收藏状态
    ProductFavoriteStatusResDTO getProductFavoriteStatus(Long userId, Long productId);
}
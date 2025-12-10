// File: milktea-backend/src/main/java/com.milktea.app/controller/ProductV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.product.ProductDetailResDTO;
import com.milktea.app.dto.product.ProductFavoriteStatusResDTO;
import com.milktea.app.dto.product.ProductListReqDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import com.milktea.app.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products") // Base path for product module
@RequiredArgsConstructor
@Slf4j
public class ProductV1Controller {

    private final ProductService productService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return principal != null ? Long.parseLong(principal.getUsername()) : null;
    }

    @GetMapping // Matches /products (used for generic list with filters)
    public ApiResponse<ProductListResDTO> getProducts(@Valid @ModelAttribute ProductListReqDTO reqDTO,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "20") Integer limit) {
        log.info("Fetching product list with filters: {}", reqDTO);
        Sort.Direction direction = (reqDTO.getSort() != null && reqDTO.getSort().endsWith("_desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (reqDTO.getSort() != null && reqDTO.getSort().contains("_")) ? reqDTO.getSort().substring(0, reqDTO.getSort().indexOf("_")) : reqDTO.getSort();
        Pageable pageable = PaginationUtil.createPageable(page, limit, sortProperty, direction);
        ProductListResDTO resDTO = productService.getProducts(reqDTO, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/categories/{categoryId}/products") // New: 2.2.2 获取分类商品
    public ApiResponse<ProductListResDTO> getCategoryProducts(@PathVariable Long categoryId,
                                                              @Valid @ModelAttribute ProductListReqDTO reqDTO, // Reuse ProductListReqDTO for sort/filter
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "20") Integer limit) {
        log.info("Fetching products for category {} with filters: {}", categoryId, reqDTO);
        Sort.Direction direction = (reqDTO.getSort() != null && reqDTO.getSort().endsWith("_desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (reqDTO.getSort() != null && reqDTO.getSort().contains("_")) ? reqDTO.getSort().substring(0, reqDTO.getSort().indexOf("_")) : reqDTO.getSort();
        Pageable pageable = PaginationUtil.createPageable(page, limit, sortProperty, direction);
        ProductListResDTO resDTO = productService.getCategoryProducts(categoryId, reqDTO, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/{productId}") // Matches /products/{id}
    public ApiResponse<ProductDetailResDTO> getProductDetail(@AuthenticationPrincipal User principal,
                                                             @PathVariable Long productId) {
        Long userId = getUserId(principal);
        log.info("Fetching product detail for ID {} by user {}", productId, userId);
        ProductDetailResDTO resDTO = productService.getProductDetail(userId, productId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{productId}/favorite") // Matches /products/{id}/favorite
    public ApiResponse<Void> addFavoriteProduct(@AuthenticationPrincipal User principal,
                                                @PathVariable Long productId) {
        Long userId = getUserId(principal);
        log.info("User {} adding product {} to favorites", userId, productId);
        productService.addFavoriteProduct(userId, productId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{productId}/favorite") // Matches /products/{id}/favorite
    public ApiResponse<Void> removeFavoriteProduct(@AuthenticationPrincipal User principal,
                                                   @PathVariable Long productId) {
        Long userId = getUserId(principal);
        log.info("User {} removing product {} from favorites", userId, productId);
        productService.removeFavoriteProduct(userId, productId);
        return ApiResponse.success();
    }

    @GetMapping("/{productId}/favorite/status") // New: 2.3.4 获取收藏状态
    public ApiResponse<ProductFavoriteStatusResDTO> getProductFavoriteStatus(@AuthenticationPrincipal User principal,
                                                                             @PathVariable Long productId) {
        Long userId = getUserId(principal);
        log.info("Checking if product {} is favorited by user {}", productId, userId);
        ProductFavoriteStatusResDTO resDTO = productService.getProductFavoriteStatus(userId, productId);
        return ApiResponse.success(resDTO);
    }
}
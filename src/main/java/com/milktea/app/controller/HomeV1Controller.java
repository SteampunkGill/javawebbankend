// File: milktea-backend/src/main/java/com.milktea.app/controller/HomeV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import com.milktea.app.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/home") // Base path for home module
@RequiredArgsConstructor
@Slf4j
public class HomeV1Controller {

    private final HomeService homeService;

    @GetMapping("/index") // Matches /home/index
    public ApiResponse<HomePageResDTO> getHomePageData(@RequestParam(required = false) BigDecimal latitude,
                                                       @RequestParam(required = false) BigDecimal longitude) {
        log.info("Fetching home page data for location: ({}, {})", latitude, longitude);
        HomePageResDTO resDTO = homeService.getHomePageData(latitude, longitude);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/recommend") // Matches /home/recommend
    public ApiResponse<ProductListResDTO> getRecommendedProducts(@RequestParam(defaultValue = "10") Integer limit,
                                                                 @RequestParam(defaultValue = "product") String type) {
        log.info("Fetching recommended products with limit {} and type {}", limit, type);
        ProductListResDTO resDTO = homeService.getRecommendedProducts(limit, type);
        return ApiResponse.success(resDTO);
    }
}
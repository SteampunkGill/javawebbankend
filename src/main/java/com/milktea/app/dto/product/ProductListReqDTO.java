package com.milktea.app.dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListReqDTO {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort;
    private String filter;
}
// File: milktea-backend/src/main/java/com.milktea.app/dto/search/ProductSearchReqDTO.java
package com.milktea.app.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchReqDTO {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort; // relevance, sales, price_asc, price_desc
    private Integer page;
    private Integer limit;
}
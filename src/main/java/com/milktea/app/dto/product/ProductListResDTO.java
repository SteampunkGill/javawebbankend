package com.milktea.app.dto.product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductListResDTO {
    private List<ProductItemDTO> products;
    private int total;
    private int page;
    private int limit;

    @Data
    public static class ProductItemDTO {
        private String id; // 如果是 String 类型，就需要转换
        private String name;
        private String image;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer sales;
        private List<String> tags;
        private String description;
    }
}
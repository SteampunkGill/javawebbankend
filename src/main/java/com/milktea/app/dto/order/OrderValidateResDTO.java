// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderValidateResDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderValidateResDTO {
    private Boolean isValid;
    private List<InvalidItemDTO> invalidItems;
    private List<WarningDTO> warnings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvalidItemDTO {
        private Long itemId;
        private String reason; // stock_out, price_changed, product_offline
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningDTO {
        private String type; // time_limit
        private String message;
    }
}
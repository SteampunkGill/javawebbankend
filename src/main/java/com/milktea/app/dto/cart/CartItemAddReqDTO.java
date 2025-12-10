// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/CartItemAddReqDTO.java
package com.milktea.app.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemAddReqDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @Min(value = 1, message = "购买数量不能小于1")
    private Integer quantity;
    private CustomizationDTO choices; // 改为使用共享的CustomizationDTO
}
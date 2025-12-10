// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/CartItemUpdateReqDTO.java
package com.milktea.app.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpdateReqDTO {
    @Min(value = 1, message = "购买数量不能小于1")
    private Integer quantity;
    private CustomizationDTO choices; // 改为使用共享的CustomizationDTO
}
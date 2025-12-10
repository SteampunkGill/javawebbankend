// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/ToppingItemDTO.java
package com.milktea.app.dto.cart;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToppingItemDTO {
    private String id;
    @Min(value = 1, message = "加料数量不能小于1")
    private Integer quantity;
}
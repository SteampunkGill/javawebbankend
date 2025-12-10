// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/CartBatchOperationReqDTO.java
package com.milktea.app.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartBatchOperationReqDTO {
    @NotBlank(message = "操作类型不能为空")
    private String action; // select_all, unselect_all, delete_selected
    @NotEmpty(message = "购物车项ID列表不能为空", groups = {SpecificOperation.class}) // For delete_selected etc.
    private List<Long> itemIds;

    // Validation groups
    public interface SpecificOperation {}
}
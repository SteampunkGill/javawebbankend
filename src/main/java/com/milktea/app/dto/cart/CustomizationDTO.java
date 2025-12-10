// File: milktea-backend/src/main/java/com.milktea.app/dto/cart/CustomizationDTO.java
package com.milktea.app.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationDTO {
    private String sweetness;
    private String temperature;
    private List<ToppingItemDTO> toppings;
}


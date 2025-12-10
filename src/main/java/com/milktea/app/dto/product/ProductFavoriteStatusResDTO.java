// File: milktea-backend/src/main/java/com.milktea.app/dto/product/ProductFavoriteStatusResDTO.java
package com.milktea.app.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavoriteStatusResDTO {
    private Boolean isFavorite;
    private Long favoriteId; // The ID of the UserFavoriteProductEntity if favorited
}
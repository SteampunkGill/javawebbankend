// File: milktea-backend/src/main/java/com.milktea.app/dto/search/ProductSearchResDTO.java
package com.milktea.app.dto.search;

import com.milktea.app.dto.category.CategoryTreeResDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResDTO {
    private List<ProductListResDTO.ProductItemDTO> products;
    private Integer total;
    private List<String> suggestions;
    private List<CategoryTreeResDTO> relatedCategories;
}
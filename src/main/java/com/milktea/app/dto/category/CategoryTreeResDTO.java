// File: milktea-backend/src/main/java/com.milktea.app/dto/category/CategoryTreeResDTO.java
package com.milktea.app.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeResDTO {
    private Long id;
    private Long parentId; // Null for top-level categories
    private String name;
    private String icon;
    private String image;
    private Integer sort;
    private String description;
    private Integer productCount;
    private List<CategoryTreeResDTO> children;
}
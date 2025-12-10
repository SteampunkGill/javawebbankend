// File: milktea-backend/src/main/java/com.milktea.app/service/CategoryService.java
package com.milktea.app.service;

import com.milktea.app.dto.category.CategoryTreeResDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeResDTO> getCategoryTree();
}
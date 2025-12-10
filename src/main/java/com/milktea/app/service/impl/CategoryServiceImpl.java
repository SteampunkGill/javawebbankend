// File: milktea-backend/src/main/java/com.milktea.app/service/impl/CategoryServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.dto.category.CategoryTreeResDTO;
import com.milktea.app.entity.CategoryEntity;
import com.milktea.app.repository.CategoryRepository;
import com.milktea.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResDTO> getCategoryTree() {
        // Fetch all active categories
        List<CategoryEntity> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();

        // Build a map for quick lookup by ID
        // Map<Long, CategoryEntity> categoryMap = allCategories.stream()
        //         .collect(Collectors.toMap(CategoryEntity::getId, category -> category));

        // Filter for top-level categories (parent_id is null)
        List<CategoryEntity> topLevelCategories = allCategories.stream()
                .filter(category -> category.getParent() == null)
                .collect(Collectors.toList());

        // Recursively build the DTO tree
        return topLevelCategories.stream()
                .map(this::mapToCategoryTreeDTO)
                .collect(Collectors.toList());
    }

    private CategoryTreeResDTO mapToCategoryTreeDTO(CategoryEntity entity) {
        CategoryTreeResDTO dto = new CategoryTreeResDTO();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        dto.setName(entity.getName());
        dto.setIcon(entity.getIconUrl());
        dto.setImage(entity.getImageUrl());
        dto.setSort(entity.getSortOrder());
        dto.setDescription(entity.getDescription());
        // For productCount, this would typically be a cached field or derived from a complex query
        dto.setProductCount(0); // Placeholder
        dto.setChildren(entity.getChildren().stream()
                .map(this::mapToCategoryTreeDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}
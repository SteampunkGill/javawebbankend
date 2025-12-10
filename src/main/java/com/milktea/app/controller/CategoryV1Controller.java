// File: milktea-backend/src/main/java/com.milktea.app/controller/CategoryV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.category.CategoryTreeResDTO;
import com.milktea.app.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryV1Controller {

    private final CategoryService categoryService;

    @GetMapping("/tree") // Matches /categories/tree
    public ApiResponse<List<CategoryTreeResDTO>> getCategoryTree() {
        log.info("Fetching category tree.");
        List<CategoryTreeResDTO> resDTO = categoryService.getCategoryTree();
        return ApiResponse.success(resDTO);
    }
}
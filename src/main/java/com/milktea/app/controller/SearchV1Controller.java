// File: milktea-backend/src/main/java/com.milktea.app/controller/SearchV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.search.ProductSearchReqDTO;
import com.milktea.app.dto.search.ProductSearchResDTO;
import com.milktea.app.dto.search.SearchHistoryReqDTO;
import com.milktea.app.dto.search.SearchHotKeywordsResDTO;
import com.milktea.app.dto.search.SearchSuggestResDTO;
import com.milktea.app.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/search") // Base path for search module
@RequiredArgsConstructor
@Slf4j
public class SearchV1Controller {

    private final SearchService searchService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return principal != null ? Long.parseLong(principal.getUsername()) : null;
    }

    @GetMapping("/products") // Matches /search/products
    public ApiResponse<ProductSearchResDTO> searchProducts(@AuthenticationPrincipal User principal,
                                                           @Valid @ModelAttribute ProductSearchReqDTO reqDTO,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = getUserId(principal);
        log.info("Searching products for user {} with keyword: {}", userId, reqDTO.getKeyword());
        Sort.Direction direction = (reqDTO.getSort() != null && reqDTO.getSort().endsWith("_desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (reqDTO.getSort() != null && reqDTO.getSort().contains("_")) ? reqDTO.getSort().substring(0, reqDTO.getSort().indexOf("_")) : reqDTO.getSort();
        Pageable pageable = PaginationUtil.createPageable(page, limit, sortProperty, direction);
        ProductSearchResDTO resDTO = searchService.searchProducts(userId, reqDTO, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/hot") // Matches /search/hot
    public ApiResponse<SearchHotKeywordsResDTO> getHotKeywordsAndHistory(@AuthenticationPrincipal User principal,
                                                                         @RequestParam(defaultValue = "10") Integer limit) { // Added limit param
        Long userId = getUserId(principal);
        log.info("Getting hot keywords and search history for user: {} with limit {}", userId, limit);
        SearchHotKeywordsResDTO resDTO = searchService.getHotKeywordsAndHistory(userId, limit);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/suggest") // Matches /search/suggest
    public ApiResponse<SearchSuggestResDTO> getSearchSuggestions(@RequestParam("keyword") String keyword, // Renamed param to keyword
                                                                 @RequestParam(defaultValue = "5") Integer limit) { // Added limit param
        log.info("Getting search suggestions for keyword: {} with limit {}", keyword, limit);
        SearchSuggestResDTO resDTO = searchService.getSearchSuggestions(keyword, limit);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/history") // Matches /search/history
    public ApiResponse<Void> addSearchHistory(@AuthenticationPrincipal User principal,
                                              @Valid @RequestBody SearchHistoryReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Adding search history for user {}: {}", userId, reqDTO.getKeyword());
        searchService.addSearchHistory(userId, reqDTO);
        return ApiResponse.success();
    }

    @DeleteMapping("/history") // Matches /search/history
    public ApiResponse<Void> clearSearchHistory(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Clearing search history for user {}", userId);
        searchService.clearSearchHistory(userId);
        return ApiResponse.success();
    }
}
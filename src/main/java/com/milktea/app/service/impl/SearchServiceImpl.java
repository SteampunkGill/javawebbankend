// File: milktea-backend/src/main/java/com.milktea.app.service/impl/SearchServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.category.CategoryTreeResDTO;
import com.milktea.app.dto.product.ProductListResDTO;
import com.milktea.app.dto.search.ProductSearchReqDTO;
import com.milktea.app.dto.search.ProductSearchResDTO;
import com.milktea.app.dto.search.SearchHistoryReqDTO;
import com.milktea.app.dto.search.SearchHotKeywordsResDTO;
import com.milktea.app.dto.search.SearchSuggestResDTO;
import com.milktea.app.entity.CategoryEntity;
import com.milktea.app.entity.ProductEntity;
import com.milktea.app.entity.SearchKeywordEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.entity.UserSearchHistoryEntity;
import com.milktea.app.repository.CategoryRepository;
import com.milktea.app.repository.ProductRepository;
import com.milktea.app.repository.SearchKeywordRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.repository.UserSearchHistoryRepository;
import com.milktea.app.service.SearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SearchKeywordRepository searchKeywordRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResDTO searchProducts(Long userId, ProductSearchReqDTO reqDTO, Pageable pageable) {
        // Record search history
        if (userId != null && reqDTO.getKeyword() != null && !reqDTO.getKeyword().trim().isEmpty()) {
            addSearchHistory(userId, new SearchHistoryReqDTO(reqDTO.getKeyword(), "product"));
        }
        if (reqDTO.getKeyword() != null && !reqDTO.getKeyword().trim().isEmpty()) {
            updateSearchKeywordCount(reqDTO.getKeyword());
        }

        // Apply sorting
        Sort sort = Sort.unsorted();
        if (reqDTO.getSort() != null) {
            switch (reqDTO.getSort()) {
                case "sales":
                    sort = Sort.by(Sort.Direction.DESC, "sales");
                    break;
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "relevance": // Default or no specific field
                default:
                    sort = Sort.by(Sort.Direction.DESC, "rating"); // Example default sort for relevance
                    break;
            }
        }
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<ProductEntity> productPage = productRepository.searchProducts(
                reqDTO.getKeyword(),
                reqDTO.getCategoryId(),
                reqDTO.getMinPrice(),
                reqDTO.getMaxPrice(),
                pageable
        );

        List<ProductListResDTO.ProductItemDTO> productItemDTOs = productPage.getContent().stream()
                .map(this::mapToProductItemDTO)
                .collect(Collectors.toList());

        ProductSearchResDTO resDTO = new ProductSearchResDTO();
        resDTO.setProducts(productItemDTOs);
        resDTO.setTotal((int) productPage.getTotalElements());

        // Search suggestions (can be derived from keyword and related products/categories)
        resDTO.setSuggestions(getSearchSuggestions(reqDTO.getKeyword(), 5).getSuggestions()); // Default limit 5

        // Related categories (if keyword is broad or no category filter applied)
        resDTO.setRelatedCategories(findRelatedCategories(reqDTO.getKeyword()));

        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchHotKeywordsResDTO getHotKeywordsAndHistory(Long userId, Integer limit) {
        SearchHotKeywordsResDTO resDTO = new SearchHotKeywordsResDTO();

        // Hot keywords
        List<SearchKeywordEntity> hotKeywords = searchKeywordRepository.findByTypeOrderByCountDesc("hot");
        resDTO.setKeywords(hotKeywords.stream()
                .limit(limit != null ? limit : 10) // Apply limit from param or default 10
                .map(k -> new SearchHotKeywordsResDTO.KeywordDTO(k.getKeyword(), k.getCount(), k.getType()))
                .collect(Collectors.toList()));

        // User search history
        List<String> userHistory = new ArrayList<>();
        if (userId != null) {
            userHistory = userSearchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(UserSearchHistoryEntity::getKeyword)
                    .distinct() // Remove duplicates if any
                    .limit(10) // Limit to latest 10 (hardcoded or configurable)
                    .collect(Collectors.toList());
        }
        resDTO.setHistory(userHistory);
        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public SearchSuggestResDTO getSearchSuggestions(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new SearchSuggestResDTO(Collections.emptyList());
        }
        List<SearchKeywordEntity> suggestedKeywords = searchKeywordRepository.findTop5ByKeywordStartingWithOrderByCountDesc(keyword); // Default 5
        List<String> suggestions = suggestedKeywords.stream()
                .limit(limit != null ? limit : 5) // Apply limit from param or default 5
                .map(SearchKeywordEntity::getKeyword)
                .collect(Collectors.toList());
        return new SearchSuggestResDTO(suggestions);
    }

    @Override
    @Transactional
    public void addSearchHistory(Long userId, SearchHistoryReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        // Limit history size to prevent too many entries
        List<UserSearchHistoryEntity> existingHistory = userSearchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (existingHistory.size() >= 50) { // Keep max 50 entries per user
            userSearchHistoryRepository.delete(existingHistory.get(existingHistory.size() - 1));
        }

        UserSearchHistoryEntity newHistory = new UserSearchHistoryEntity();
        newHistory.setUser(user);
        newHistory.setKeyword(reqDTO.getKeyword());
        newHistory.setType(reqDTO.getType());
        newHistory.setCreatedAt(Instant.now());
        userSearchHistoryRepository.save(newHistory);
        log.info("Added search history for user {}: {}", userId, reqDTO.getKeyword());
    }

    @Override
    @Transactional
    public void clearSearchHistory(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        userSearchHistoryRepository.deleteByUserId(userId);
        log.info("Cleared search history for user {}", userId);
    }

    private void updateSearchKeywordCount(String keyword) {
        Optional<SearchKeywordEntity> existingKeyword = searchKeywordRepository.findByKeyword(keyword);
        SearchKeywordEntity searchKeyword = existingKeyword.orElseGet(SearchKeywordEntity::new);
        searchKeyword.setKeyword(keyword);
        searchKeyword.setCount(searchKeyword.getCount() + 1);
        if (searchKeyword.getId() == null) {
            searchKeyword.setType("new"); // Mark as new initially
            searchKeyword.setCreatedAt(Instant.now());
        }
        searchKeyword.setUpdatedAt(Instant.now());
        searchKeywordRepository.save(searchKeyword);
    }

    private ProductListResDTO.ProductItemDTO mapToProductItemDTO(ProductEntity entity) {
        ProductListResDTO.ProductItemDTO dto = new ProductListResDTO.ProductItemDTO();
        dto.setId(String.valueOf(entity.getId())); // 修复：将 Long 转换为 String
        dto.setName(entity.getName());
        dto.setImage(entity.getMainImageUrl());
        dto.setPrice(entity.getPrice());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setSales(entity.getSales());
        if (entity.getTags() != null) {
            try {
                dto.setTags(objectMapper.readValue(entity.getTags(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                log.error("Failed to parse product tags for product {}: {}", entity.getId(), e.getMessage());
                dto.setTags(new ArrayList<>());
            }
        } else {
            dto.setTags(new ArrayList<>());
        }
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private List<CategoryTreeResDTO> findRelatedCategories(String keyword) {
        // Simple heuristic: search categories whose name contains the keyword
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<CategoryEntity> matchingCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .limit(3) // Limit results
                .collect(Collectors.toList());

        return matchingCategories.stream()
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
        dto.setProductCount(0); // Placeholder
        dto.setChildren(Collections.emptyList()); // For related categories, usually don't need deep children
        return dto;
    }
}
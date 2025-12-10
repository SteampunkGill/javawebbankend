// File: milktea-backend/src/main/java/com.milktea.app/service/SearchService.java
package com.milktea.app.service;

import com.milktea.app.dto.search.ProductSearchReqDTO;
import com.milktea.app.dto.search.ProductSearchResDTO;
import com.milktea.app.dto.search.SearchHistoryReqDTO;
import com.milktea.app.dto.search.SearchHotKeywordsResDTO;
import com.milktea.app.dto.search.SearchSuggestResDTO;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    ProductSearchResDTO searchProducts(Long userId, ProductSearchReqDTO reqDTO, Pageable pageable);
    SearchHotKeywordsResDTO getHotKeywordsAndHistory(Long userId, Integer limit); // Added limit parameter
    SearchSuggestResDTO getSearchSuggestions(String keyword, Integer limit); // Renamed prefix to keyword, added limit
    void addSearchHistory(Long userId, SearchHistoryReqDTO reqDTO);
    void clearSearchHistory(Long userId);
}
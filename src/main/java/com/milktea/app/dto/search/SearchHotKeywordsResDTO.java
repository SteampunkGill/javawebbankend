// File: milktea-backend/src/main/java/com.milktea.app/dto/search/SearchHotKeywordsResDTO.java
package com.milktea.app.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHotKeywordsResDTO {
    private List<KeywordDTO> keywords;
    private List<String> history;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordDTO {
        private String word;
        private Integer count;
        private String type; // hot, new
    }
}
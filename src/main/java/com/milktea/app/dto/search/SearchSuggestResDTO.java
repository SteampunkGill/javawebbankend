// File: milktea-backend/src/main/java/com.milktea.app/dto/search/SearchSuggestResDTO.java
package com.milktea.app.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestResDTO {
    private List<String> suggestions;
}
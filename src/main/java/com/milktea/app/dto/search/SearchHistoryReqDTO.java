// File: milktea-backend/src/main/java/com.milktea.app/dto/search/SearchHistoryReqDTO.java
package com.milktea.app.dto.search;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryReqDTO {
    @NotBlank(message = "搜索词不能为空")
    private String keyword;
    private String type; // product, category
}
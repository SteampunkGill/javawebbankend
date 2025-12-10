// File: milktea-backend/src/main/java/com.milktea.app/dto/system/PageConfigResDTO.java
package com.milktea.app.dto.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageConfigResDTO {
    private String pageName;
    private List<ConfigItemDTO> configItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigItemDTO {
        private String key;
        private String value;
    }
}
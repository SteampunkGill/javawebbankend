// File: milktea-backend/src/main/java/com.milktea.app/dto/point/PointTransactionListResDTO.java
package com.milktea.app.dto.point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionListResDTO {
    private List<PointTransactionDTO> transactions;
    private Integer total;
    private Integer page;
    private Integer limit;
    private PointSummaryDTO summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointTransactionDTO {
        private Long id;
        private String type;
        private Integer points;
        private Integer balance;
        private String description;
        private String relatedId;
        private String relatedType;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointSummaryDTO {
        private Integer totalPoints;
        private Integer availablePoints;
        private Integer frozenPoints;
        private Integer expiringPoints;
        private LocalDate expireDate;
    }
}
// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderReviewReqDTO.java
package com.milktea.app.dto.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewReqDTO {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;
    private String content;
    private List<String> images;
    private List<String> tags;
    private Boolean anonymous;
}
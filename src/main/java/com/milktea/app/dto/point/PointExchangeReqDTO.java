// File: milktea-backend/src/main/java/com.milktea.app/dto/point/PointExchangeReqDTO.java
package com.milktea.app.dto.point;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointExchangeReqDTO {
    @NotNull(message = "兑换项ID不能为空")
    private Long itemId;
    @Min(value = 1, message = "兑换数量不能小于1")
    private Integer quantity;
}
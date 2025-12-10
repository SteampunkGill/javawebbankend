// File: milktea-backend/src/main/java/com.milktea.app/dto/store/StoreNearbyReqDTO.java
package com.milktea.app.dto.store;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreNearbyReqDTO {
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;
    @Min(value = 100, message = "查询半径不能小于100米")
    @Max(value = 100000, message = "查询半径不能大于100公里")
    private Integer radius; // In meters
    @Min(value = 1, message = "查询数量不能小于1")
    private Integer limit;
}
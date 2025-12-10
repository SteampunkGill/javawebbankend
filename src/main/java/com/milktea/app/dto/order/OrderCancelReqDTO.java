// File: milktea-backend/src/main/java/com.milktea.app/dto/order/OrderCancelReqDTO.java
package com.milktea.app.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelReqDTO {
    private String reason;
    private String remark;
}
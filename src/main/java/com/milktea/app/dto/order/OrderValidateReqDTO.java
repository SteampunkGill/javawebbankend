package com.milktea.app.dto.order;

import lombok.Data;
import java.util.List;

@Data
public class OrderValidateReqDTO {
    private List<Long> itemIds;
    private Long addressId;
    private Long storeId;  // 添加这个字段
    private Long couponId;
    private String remark;
}
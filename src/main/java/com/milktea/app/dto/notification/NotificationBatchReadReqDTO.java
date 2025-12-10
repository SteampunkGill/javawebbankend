// File: milktea-backend/src/main/java/com.milktea.app/dto/notification/NotificationBatchReadReqDTO.java
package com.milktea.app.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchReadReqDTO {
    private List<Long> ids;
    private Boolean all;
}
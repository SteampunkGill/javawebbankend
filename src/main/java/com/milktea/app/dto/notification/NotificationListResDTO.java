// File: milktea-backend/src/main/java/com.milktea.app/dto/notification/NotificationListResDTO.java
package com.milktea.app.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResDTO {
    private List<NotificationDTO> notifications;
    private Integer unreadCount;
    private Integer total;
    private Integer page;
    private Integer limit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDTO {
        private Long id;
        private String type;
        private String title;
        private String content;
        private Map<String, Object> data; // For additional JSON data
        private Boolean isRead;
        private Instant createdAt;
        private Instant readAt;
    }
}
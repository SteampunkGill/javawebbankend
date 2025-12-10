// File: milktea-backend/src/main/java/com.milktea.app/service/NotificationService.java
package com.milktea.app.service;

import com.milktea.app.dto.notification.NotificationBatchReadReqDTO;
import com.milktea.app.dto.notification.NotificationListResDTO;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationListResDTO getNotifications(Long userId, String type, Pageable pageable); // Added type parameter
    void markNotificationAsRead(Long userId, Long notificationId);
    void batchMarkNotificationsAsRead(Long userId, NotificationBatchReadReqDTO reqDTO);
    Long getUnreadNotificationCount(Long userId);
}
// File: milktea-backend/src/main/java/com.milktea.app/controller/NotificationV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.notification.NotificationBatchReadReqDTO;
import com.milktea.app.dto.notification.NotificationListResDTO;
import com.milktea.app.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications") // Base path for notifications module
@RequiredArgsConstructor
@Slf4j
public class NotificationV1Controller {

    private final NotificationService notificationService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @GetMapping // Matches /notifications
    public ApiResponse<NotificationListResDTO> getNotifications(@AuthenticationPrincipal User principal,
                                                                @RequestParam(required = false, defaultValue = "all") String type, // Added type parameter
                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer limit) { // Changed default limit to 20
        Long userId = getUserId(principal);
        log.info("Getting notifications for user {} with type: {}", userId, type);
        Pageable pageable = PaginationUtil.createPageable(page, limit);
        NotificationListResDTO resDTO = notificationService.getNotifications(userId, type, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadNotificationCount(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Getting unread notification count for user: {}", userId);
        Long count = notificationService.getUnreadNotificationCount(userId);
        return ApiResponse.success(count);
    }

    @PutMapping("/{notificationId}/read") // Matches /notifications/{id}/read
    public ApiResponse<Void> markNotificationAsRead(@AuthenticationPrincipal User principal,
                                                    @PathVariable("notificationId") Long notificationId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Marking notification {} as read for user {}", notificationId, userId);
        notificationService.markNotificationAsRead(userId, notificationId);
        return ApiResponse.success();
    }

    @PostMapping("/batchread") // Matches /notifications/batchread
    public ApiResponse<Void> batchMarkNotificationsAsRead(@AuthenticationPrincipal User principal,
                                                          @Valid @RequestBody NotificationBatchReadReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Batch marking notifications as read for user {}. All: {}, IDs: {}", userId, reqDTO.getAll(), reqDTO.getIds());
        notificationService.batchMarkNotificationsAsRead(userId, reqDTO);
        return ApiResponse.success();
    }
}
// File: milktea-backend/src/main/java/com.milktea.app/service/impl/NotificationServiceImpl.java
package com.milktea.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.notification.NotificationBatchReadReqDTO;
import com.milktea.app.dto.notification.NotificationListResDTO;
import com.milktea.app.entity.NotificationEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.NotificationRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationListResDTO getNotifications(Long userId, String type, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        Specification<NotificationEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (type != null && !type.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(root.get("type"), type.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Find all matching transactions for the user, then manually paginate
        // For a more efficient solution, repository method should return Page<PointTransactionEntity> directly with the spec.
        Page<NotificationEntity> notificationPage = notificationRepository.findAll(spec, pageable);


        List<NotificationListResDTO.NotificationDTO> notificationDTOs = notificationPage.getContent().stream()
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList());

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        NotificationListResDTO resDTO = new NotificationListResDTO();
        resDTO.setNotifications(notificationDTOs);
        resDTO.setUnreadCount((int) unreadCount);
        resDTO.setTotal((int) notificationPage.getTotalElements());
        resDTO.setPage(notificationPage.getNumber() + 1);
        resDTO.setLimit(pageable.getPageSize());
        return resDTO;
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long userId, Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Notification not found."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to notification.");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
            log.info("Notification {} marked as read for user {}", notificationId, userId);
        }
    }

    @Override
    @Transactional
    public void batchMarkNotificationsAsRead(Long userId, NotificationBatchReadReqDTO reqDTO) {
        List<NotificationEntity> notificationsToUpdate;
        if (reqDTO.getAll() != null && reqDTO.getAll()) {
            // Mark all unread notifications for the user as read
            notificationsToUpdate = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                    .stream()
                    .filter(n -> !n.getIsRead())
                    .collect(Collectors.toList());
        } else if (reqDTO.getIds() != null && !reqDTO.getIds().isEmpty()) {
            // Mark specific notifications as read
            notificationsToUpdate = notificationRepository.findAllById(reqDTO.getIds());
            notificationsToUpdate = notificationsToUpdate.stream()
                    .filter(n -> n.getUser().getId().equals(userId) && !n.getIsRead())
                    .collect(Collectors.toList());
        } else {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "Either 'all' must be true or 'ids' must be provided for batch read.");
        }

        notificationsToUpdate.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(Instant.now());
        });
        notificationRepository.saveAll(notificationsToUpdate);
        log.info("{} notifications marked as read for user {}", notificationsToUpdate.size(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationListResDTO.NotificationDTO mapToNotificationDTO(NotificationEntity entity) {
        NotificationListResDTO.NotificationDTO dto = new NotificationListResDTO.NotificationDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        // Parse data_json to Map
        if (entity.getDataJson() != null) {
            try {
                dto.setData(objectMapper.readValue(entity.getDataJson(), new TypeReference<>() {}));
            } catch (Exception e) {
                log.error("Failed to parse data_json for notification {}: {}", entity.getId(), e.getMessage());
                dto.setData(null);
            }
        }
        dto.setIsRead(entity.getIsRead());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());
        return dto;
    }
}
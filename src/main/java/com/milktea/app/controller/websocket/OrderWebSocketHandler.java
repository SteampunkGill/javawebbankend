// File: milktea-backend/src/main/java/com.milktea.app/controller/websocket/OrderWebSocketHandler.java
package com.milktea.app.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milktea.app.dto.order.OrderStatusChangedWsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Sends an order status update to a specific user.
     * The destination pattern is /user/{userId}/queue/orders/status
     *
     * @param userId The ID of the user to send the message to.
     * @param dto The DTO containing the order status changed information.
     */
    public void sendOrderStatusUpdate(Long userId, OrderStatusChangedWsDTO dto) {
        String destination = "/queue/orders/status"; // Relative to /user/{userId}
        log.info("Sending order status update to user {} at destination {}: {}", userId, destination, dto.getData().getNewStatus());
        // SimpMessagingTemplate automatically prepends /user/{userId} when sending to /queue
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), destination, dto);
    }

    /**
     * Sends an order status update to a general topic (e.g., for admin dashboards).
     * The destination pattern is /topic/orders/status
     *
     * @param dto The DTO containing the order status changed information.
     */
    public void broadcastOrderStatusUpdate(OrderStatusChangedWsDTO dto) {
        String destination = "/topic/orders/status";
        log.info("Broadcasting order status update to destination {}: {}", destination, dto.getData().getNewStatus());
        messagingTemplate.convertAndSend(destination, dto);
    }

    // You can also add methods to send general notifications, messages etc.
    // For example:
    // public void sendUserNotification(Long userId, String message) {
    //     messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", message);
    // }
}
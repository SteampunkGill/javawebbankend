// File: milktea-backend/src/main/java/com.milktea.app/service/OrderService.java
package com.milktea.app.service;

import com.milktea.app.dto.order.*;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface OrderService {
    CheckoutResDTO getCheckoutDetails(Long userId, CheckoutReqDTO reqDTO);
    OrderValidateResDTO validateOrder(Long userId, OrderValidateReqDTO reqDTO);
    OrderCreateResDTO createOrder(Long userId, OrderCreateReqDTO reqDTO);
    // New: 4.1.2 立即购买
    OrderCreateResDTO buyNow(Long userId, OrderCreateReqDTO reqDTO);
    PaymentResDTO initiatePayment(Long userId, Long orderId, PaymentReqDTO reqDTO);
    PaymentStatusResDTO getPaymentStatus(Long userId, Long orderId);
    // New: 4.2.3 取消支付
    void cancelPayment(Long userId, Long orderId);
    OrderListResDTO getUserOrders(Long userId, String status, String type, Instant startDate, Instant endDate, Pageable pageable); // Added type, startDate, endDate
    OrderDetailResDTO getOrderDetail(Long userId, Long orderId);
    void cancelOrder(Long userId, Long orderId, OrderCancelReqDTO reqDTO);
    void confirmOrder(Long userId, Long orderId); // For pickup/delivery confirmation
    // New: 4.3.3.3 催单提醒
    void remindOrder(Long userId, Long orderId);
    void applyOrderRefund(Long userId, Long orderId, OrderRefundApplyReqDTO reqDTO);
    void addOrderReview(Long userId, Long orderId, OrderReviewReqDTO reqDTO);
    // WebSocket related
    void publishOrderStatusUpdate(Long orderId, String newStatus, String statusText, Instant estimatedTime);
}
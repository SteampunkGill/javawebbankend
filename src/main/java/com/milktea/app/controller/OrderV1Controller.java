// File: milktea-backend/src/main/java/com.milktea.app/controller/OrderV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.order.*;
import com.milktea.app.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import com.milktea.app.common.exception.BusinessException;
import java.time.Instant; // Added for startDate/endDate
import java.time.format.DateTimeParseException; // Added for parsing dates

@RestController
@RequestMapping("/v1/orders") // Base path for order module
@RequiredArgsConstructor
@Slf4j
public class OrderV1Controller {

    private final OrderService orderService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @PostMapping("/checkout") // Matches /cart/checkout
    public ApiResponse<CheckoutResDTO> getCheckoutDetails(@AuthenticationPrincipal User principal,
                                                          @Valid @RequestBody CheckoutReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Getting checkout details for user {}: {}", userId, reqDTO.getItemIds());
        CheckoutResDTO resDTO = orderService.getCheckoutDetails(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/validate") // Matches /cart/validate
    public ApiResponse<OrderValidateResDTO> validateOrder(@AuthenticationPrincipal User principal,
                                                          @Valid @RequestBody OrderValidateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Validating order for user {}: {}", userId, reqDTO.getItemIds());
        OrderValidateResDTO resDTO = orderService.validateOrder(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping // Matches /orders
    public ApiResponse<OrderCreateResDTO> createOrder(@AuthenticationPrincipal User principal,
                                                      @Valid @RequestBody OrderCreateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Creating order for user {}: {}", userId, reqDTO.getType());
        OrderCreateResDTO resDTO = orderService.createOrder(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/buy-now") // New: 4.1.2 立即购买
    public ApiResponse<OrderCreateResDTO> buyNow(@AuthenticationPrincipal User principal,
                                                 @Valid @RequestBody OrderCreateReqDTO reqDTO) { // Uses same DTO as create order
        Long userId = getUserId(principal);
        log.info("Buy now order for user {}: {}", userId, reqDTO.getItems().get(0).getProductId());
        OrderCreateResDTO resDTO = orderService.buyNow(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{orderId}/pay") // Matches /orders/{id}/pay
    public ApiResponse<PaymentResDTO> initiatePayment(@AuthenticationPrincipal User principal,
                                                      @PathVariable("orderId") Long orderId, // Renamed path variable for clarity
                                                      @Valid @RequestBody PaymentReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Initiating payment for order {} by user {}", orderId, userId);
        PaymentResDTO resDTO = orderService.initiatePayment(userId, orderId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/{orderId}/pay/status") // Matches /orders/{id}/pay/status
    public ApiResponse<PaymentStatusResDTO> getPaymentStatus(@AuthenticationPrincipal User principal,
                                                             @PathVariable("orderId") Long orderId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Getting payment status for order {} by user {}", orderId, userId);
        PaymentStatusResDTO resDTO = orderService.getPaymentStatus(userId, orderId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{orderId}/pay/cancel") // New: 4.2.3 取消支付
    public ApiResponse<Void> cancelPayment(@AuthenticationPrincipal User principal,
                                           @PathVariable("orderId") Long orderId) {
        Long userId = getUserId(principal);
        log.info("Cancelling payment for order {} by user {}", orderId, userId);
        orderService.cancelPayment(userId, orderId);
        return ApiResponse.success();
    }

    @GetMapping({"", "/"})
    public ApiResponse<OrderListResDTO> getUserOrders(@AuthenticationPrincipal User principal,
                                                      @RequestParam(required = false, defaultValue = "all") String status,
                                                      @RequestParam(required = false, defaultValue = "all") String type, // Added type parameter
                                                      @RequestParam(required = false) String startDate, // Added startDate
                                                      @RequestParam(required = false) String endDate,   // Added endDate
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getUserId(principal);
        log.info("Getting orders for user {} with status {}, type {}, startDate {}, endDate {}", userId, status, type, startDate, endDate);

        Instant startInstant = null;
        Instant endInstant = null;
        try {
            if (startDate != null) {
                startInstant = Instant.parse(startDate + "T00:00:00Z"); // Assuming start of day UTC
            }
            if (endDate != null) {
                endInstant = Instant.parse(endDate + "T23:59:59Z"); // Assuming end of day UTC
            }
        } catch (DateTimeParseException e) {
            log.error("Error parsing date parameters: {}", e.getMessage());
            throw new BusinessException(com.milktea.app.common.constant.ErrorCode.INVALID_PARAM, "Invalid date format. Expected YYYY-MM-DD.");
        }


        Pageable pageable = PaginationUtil.createPageable(page, limit);
        OrderListResDTO resDTO = orderService.getUserOrders(userId, status, type, startInstant, endInstant, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/{orderId}") // Matches /orders/{id}
    public ApiResponse<OrderDetailResDTO> getOrderDetail(@AuthenticationPrincipal User principal,
                                                         @PathVariable("orderId") Long orderId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Getting order detail for order {} by user {}", orderId, userId);
        OrderDetailResDTO resDTO = orderService.getOrderDetail(userId, orderId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{orderId}/cancel") // Matches /orders/{id}/cancel
    public ApiResponse<Void> cancelOrder(@AuthenticationPrincipal User principal,
                                         @PathVariable("orderId") Long orderId, // Renamed path variable for clarity
                                         @RequestBody(required = false) OrderCancelReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Cancelling order {} by user {}", orderId, userId);
        orderService.cancelOrder(userId, orderId, reqDTO);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/confirm") // Matches /orders/{id}/confirm
    public ApiResponse<Void> confirmOrder(@AuthenticationPrincipal User principal,
                                          @PathVariable("orderId") Long orderId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Confirming order {} by user {}", orderId, userId);
        orderService.confirmOrder(userId, orderId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/remind") // New: 4.3.3.3 催单提醒
    public ApiResponse<Void> remindOrder(@AuthenticationPrincipal User principal,
                                         @PathVariable("orderId") Long orderId) {
        Long userId = getUserId(principal);
        log.info("Reminding order {} by user {}", orderId, userId);
        orderService.remindOrder(userId, orderId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/refund/apply") // Matches /orders/{id}/refund/apply
    public ApiResponse<Void> applyOrderRefund(@AuthenticationPrincipal User principal,
                                              @PathVariable("orderId") Long orderId, // Renamed path variable for clarity
                                              @Valid @RequestBody OrderRefundApplyReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Applying refund for order {} by user {}", orderId, userId);
        orderService.applyOrderRefund(userId, orderId, reqDTO);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/rate") // Matches /orders/{id}/rate
    public ApiResponse<Void> addOrderReview(@AuthenticationPrincipal User principal,
                                            @PathVariable("orderId") Long orderId, // Renamed path variable for clarity
                                            @Valid @RequestBody OrderReviewReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Adding review for order {} by user {}", orderId, userId);
        orderService.addOrderReview(userId, orderId, reqDTO);
        return ApiResponse.success();
    }
}
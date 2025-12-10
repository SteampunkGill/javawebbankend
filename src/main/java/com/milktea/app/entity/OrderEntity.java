// File: milktea-backend/src/main/java/com/milktea/app/entity/OrderEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // delivery, pickup

    @Column(name = "status", nullable = false, length = 20)
    private String status; // created, paid, making, ready, completed, cancelled, refunded

    @Column(name = "status_text", length = 50)
    private String statusText;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;

    @Column(name = "product_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal productAmount;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "package_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal packageFee;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "points_discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsDiscountAmount;

    @Column(name = "balance_discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceDiscountAmount;

    @Column(name = "points_used", nullable = false)
    private Integer pointsUsed;

    @Column(name = "balance_used", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceUsed;

    @Column(name = "coupon_id")
    private Long couponId; // Note: DDL mentions avoiding FK here due to potential circular dependency

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private UserAddressEntity deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_store_id")
    private StoreEntity pickupStore;

    @Column(name = "delivery_time_expected")
    private Instant deliveryTimeExpected;

    @Column(name = "remark")
    private String remark;

    @Column(name = "invoice_type", length = 20)
    private String invoiceType;

    @Column(name = "invoice_title", length = 100)
    private String invoiceTitle;

    @Column(name = "invoice_tax_number", length = 50)
    private String invoiceTaxNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "estimated_ready_time")
    private Instant estimatedReadyTime;

    @Column(name = "estimated_arrival_time")
    private Instant estimatedArrivalTime;

    @Column(name = "rider_name", length = 50)
    private String riderName;

    @Column(name = "rider_phone", length = 20)
    private String riderPhone;

    @Column(name = "rider_longitude", precision = 10, scale = 7)
    private BigDecimal riderLongitude;

    @Column(name = "rider_latitude", precision = 10, scale = 7)
    private BigDecimal riderLatitude;

    @Column(name = "pickup_code", length = 20)
    private String pickupCode;

    @Column(name = "pickup_time_actual")
    private Instant pickupTimeActual;

    @Column(name = "counter_number", length = 20)
    private String counterNumber;

    @Column(name = "cancel_deadline")
    private Instant cancelDeadline;

    @Column(name = "refund_deadline")
    private Instant refundDeadline;

    @Column(name = "rate_deadline")
    private Instant rateDeadline;

    @Column(name = "is_rated", nullable = false)
    private Boolean isRated;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderStatusTimelineEntity> statusTimelines;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
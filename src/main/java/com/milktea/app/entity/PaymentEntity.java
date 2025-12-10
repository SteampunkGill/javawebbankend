// File: milktea-backend/src/main/java/com.milktea.app/entity/PaymentEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "pay_id", length = 100)
    private String payId; // Third-party payment order ID

    @Column(name = "transaction_id", length = 100)
    private String transactionId; // Payment platform transaction ID

    @Column(name = "pay_type", nullable = false, length = 20)
    private String payType; // alipay, wechat, balance

    @Column(name = "channel", length = 20)
    private String channel; // miniprogram, app, h5

    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;

    @Column(name = "pay_status", nullable = false, length = 20)
    private String payStatus; // unpaid, paid, failed, cancelled

    @Column(name = "pay_time")
    private Instant payTime;

    @Column(name = "expire_time")
    private Instant expireTime;

    @Column(name = "is_sandbox", nullable = false)
    private Boolean isSandbox;

    @Column(name = "payment_url")
    private String paymentUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
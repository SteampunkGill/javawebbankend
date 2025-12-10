// File: milktea-backend/src/main/java/com.milktea.app/entity/OrderStatusTimelineEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "order_status_timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusTimelineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "status_text", nullable = false, length = 50)
    private String statusText;

    @Column(name = "time", nullable = false)
    private Instant time;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
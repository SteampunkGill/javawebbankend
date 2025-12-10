// File: milktea-backend/src/main/java/com.milktea.app/entity/PointTransactionEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "point_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange;

    @Column(name = "balance_after_transaction", nullable = false)
    private Integer balanceAfterTransaction;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // earn, use

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "related_id", length = 50)
    private String relatedId;

    @Column(name = "related_type", length = 20)
    private String relatedType; // order, exchange_item

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
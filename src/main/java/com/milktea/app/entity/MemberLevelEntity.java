// File: milktea-backend/src/main/java/com/milktea/app/entity/MemberLevelEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "member_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "min_growth_value", nullable = false)
    private Integer minGrowthValue;

    @Column(name = "description")
    private String description;

    @Column(name = "privileges_json")
    @JdbcTypeCode(SqlTypes.JSON) // Use JdbcTypeCode for JSONB mapping
    private String privilegesJson; // Store as JSON string

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
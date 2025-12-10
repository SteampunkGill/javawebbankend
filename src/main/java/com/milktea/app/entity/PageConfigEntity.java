// File: milktea-backend/src/main/java/com.milktea.app/entity/PageConfigEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "page_configs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"page_name", "key"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_name", nullable = false, length = 50)
    private String pageName;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "value")
    private String value; // Can store JSON string

    @Column(name = "description")
    private String description;

    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType; // string, boolean, json, number

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
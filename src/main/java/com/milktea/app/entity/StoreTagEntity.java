// File: milktea-backend/src/main/java/com.milktea.app/entity/StoreTagEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "store_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StoreTagEntity.StoreTagId.class)
public class StoreTagEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Id
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName; // 人气门店, 支持自取

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreTagId implements Serializable {
        private Long store;
        private String tagName;
    }
}
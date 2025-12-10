// File: milktea-backend/src/main/java/com.milktea.app/entity/StoreServiceEntity.java
package com.milktea.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "store_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StoreServiceEntity.StoreServiceId.class)
public class StoreServiceEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Id
    @Column(name = "service_type", nullable = false, length = 20)
    private String serviceType; // delivery, pickup

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreServiceId implements Serializable {
        private Long store;
        private String serviceType;
    }
}
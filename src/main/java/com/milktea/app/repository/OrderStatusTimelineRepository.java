package com.milktea.app.repository;

import com.milktea.app.entity.OrderStatusTimelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // 必须导入

public interface OrderStatusTimelineRepository extends JpaRepository<OrderStatusTimelineEntity, Long> {
    List<OrderStatusTimelineEntity> findByOrderIdOrderByTimeAsc(Long orderId);
    Optional<OrderStatusTimelineEntity> findByOrderIdAndIsCurrentTrue(Long orderId);
}
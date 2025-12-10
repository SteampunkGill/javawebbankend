// File: milktea-backend/src/main/java/com.milktea.app/repository/PointTransactionRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.PointTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 添加这个导入
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransactionEntity, Long>, JpaSpecificationExecutor<PointTransactionEntity> {
    // 原有的方法保持不变
    List<PointTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 可选：添加分页版本的方法以提高性能
    Page<PointTransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
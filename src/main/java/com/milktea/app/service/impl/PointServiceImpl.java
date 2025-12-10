// File: milktea-backend/src/main/java/com.milktea.app.service/impl/PointServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.point.PointExchangeReqDTO;
import com.milktea.app.dto.point.PointTransactionListResDTO;
import com.milktea.app.entity.PointExchangeItemEntity;
import com.milktea.app.entity.PointTransactionEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.PointExchangeItemRepository;
import com.milktea.app.repository.PointTransactionRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointExchangeItemRepository pointExchangeItemRepository;

    @Override
    @Transactional(readOnly = true)
    public PointTransactionListResDTO getPointTransactions(Long userId, String type, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        // 使用 Specification 构建查询条件
        Specification<PointTransactionEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            if (type != null && !type.equalsIgnoreCase("all")) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type.toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 方法1：使用 Specification 和 Pageable 进行分页查询（推荐）
        Page<PointTransactionEntity> transactionPage = pointTransactionRepository.findAll(spec, pageable);

        // 方法2：或者使用原有的 findByUserIdOrderByCreatedAtDesc 方法（如果添加了分页版本）
        // Page<PointTransactionEntity> transactionPage = pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<PointTransactionListResDTO.PointTransactionDTO> transactionDTOs = transactionPage.getContent().stream()
                .map(this::mapToPointTransactionDTO)
                .collect(Collectors.toList());

        PointTransactionListResDTO.PointSummaryDTO summary = new PointTransactionListResDTO.PointSummaryDTO(
                user.getPoints(),
                user.getPoints(), // availablePoints same as totalPoints for now, no frozen logic
                0, // frozenPoints placeholder
                100, // expiringPoints placeholder
                LocalDate.of(2024, 12, 31) // expireDate placeholder
        );

        PointTransactionListResDTO resDTO = new PointTransactionListResDTO();
        resDTO.setTransactions(transactionDTOs);
        resDTO.setTotal((int) transactionPage.getTotalElements());
        resDTO.setPage(transactionPage.getNumber() + 1);
        resDTO.setLimit(pageable.getPageSize());
        resDTO.setSummary(summary);
        return resDTO;
    }

    @Override
    @Transactional
    public void exchangePoints(Long userId, PointExchangeReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        PointExchangeItemEntity exchangeItem = pointExchangeItemRepository.findById(reqDTO.getItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_EXCHANGE_ITEM_NOT_FOUND, "Exchange item not found."));

        if (!exchangeItem.getIsActive()) {
            throw new BusinessException(ErrorCode.POINT_EXCHANGE_ITEM_NOT_FOUND, "Exchange item is not active.");
        }
        if (exchangeItem.getStock() != null && exchangeItem.getStock() < reqDTO.getQuantity()) {
            throw new BusinessException(ErrorCode.POINT_EXCHANGE_ITEM_OUT_OF_STOCK, "Exchange item stock insufficient.");
        }

        int totalCost = exchangeItem.getPointsCost() * reqDTO.getQuantity();
        if (user.getPoints() < totalCost) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINTS, "Insufficient points to exchange.");
        }

        // Deduct points
        user.setPoints(user.getPoints() - totalCost);
        userRepository.save(user);

        // Update item stock if applicable
        if (exchangeItem.getStock() != null) {
            exchangeItem.setStock(exchangeItem.getStock() - reqDTO.getQuantity());
            pointExchangeItemRepository.save(exchangeItem);
        }

        // Record point transaction
        PointTransactionEntity transaction = new PointTransactionEntity();
        transaction.setUser(user);
        transaction.setPointsChange(-totalCost);
        transaction.setBalanceAfterTransaction(user.getPoints());
        transaction.setType("use");
        transaction.setDescription("兑换商品: " + exchangeItem.getName());
        transaction.setRelatedType("exchange_item");
        transaction.setRelatedId(String.valueOf(exchangeItem.getId()));
        transaction.setCreatedAt(Instant.now());
        pointTransactionRepository.save(transaction);

        // Trigger action based on target_type (e.g., issue a coupon, create an order for a product)
        log.info("User {} exchanged {} points for item {}. Target type: {}", userId, totalCost, exchangeItem.getId(), exchangeItem.getTargetType());
        // Placeholder for actual logic:
        // if ("coupon_template".equals(exchangeItem.getTargetType())) {
        //     couponService.receiveCoupon(userId, exchangeItem.getTargetId());
        // } else if ("product".equals(exchangeItem.getTargetType())) {
        //     // Logic to create a special order for the redeemed product
        // }
    }

    private PointTransactionListResDTO.PointTransactionDTO mapToPointTransactionDTO(PointTransactionEntity entity) {
        PointTransactionListResDTO.PointTransactionDTO dto = new PointTransactionListResDTO.PointTransactionDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setPoints(entity.getPointsChange());
        dto.setBalance(entity.getBalanceAfterTransaction());
        dto.setDescription(entity.getDescription());
        dto.setRelatedId(entity.getRelatedId());
        dto.setRelatedType(entity.getRelatedType());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
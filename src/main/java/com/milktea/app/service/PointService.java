// File: milktea-backend/src/main/java/com.milktea.app/service/PointService.java
package com.milktea.app.service;

import com.milktea.app.dto.point.PointExchangeReqDTO;
import com.milktea.app.dto.point.PointTransactionListResDTO;
import org.springframework.data.domain.Pageable;

public interface PointService {
    PointTransactionListResDTO getPointTransactions(Long userId, String type, Pageable pageable); // Added type parameter
    void exchangePoints(Long userId, PointExchangeReqDTO reqDTO);
}
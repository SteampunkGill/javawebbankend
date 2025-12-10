// File: milktea-backend/src/main/java/com.milktea.app/controller/PointV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.point.PointExchangeReqDTO;
import com.milktea.app.dto.point.PointTransactionListResDTO;
import com.milktea.app.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/points") // Base path for points module
@RequiredArgsConstructor
@Slf4j
public class PointV1Controller {

    private final PointService pointService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @GetMapping("/transactions") // Matches /points/transactions
    public ApiResponse<PointTransactionListResDTO> getPointTransactions(@AuthenticationPrincipal User principal,
                                                                        @RequestParam(required = false, defaultValue = "all") String type, // Added type parameter
                                                                        @RequestParam(defaultValue = "1") Integer page,
                                                                        @RequestParam(defaultValue = "20") Integer limit) { // Changed default limit to 20
        Long userId = getUserId(principal);
        log.info("Getting point transactions for user {} with type: {}", userId, type);
        Pageable pageable = PaginationUtil.createPageable(page, limit);
        PointTransactionListResDTO resDTO = pointService.getPointTransactions(userId, type, pageable);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/exchange") // Matches /points/exchange
    public ApiResponse<Void> exchangePoints(@AuthenticationPrincipal User principal,
                                            @Valid @RequestBody PointExchangeReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("User {} exchanging points for item {}: quantity {}", userId, reqDTO.getItemId(), reqDTO.getQuantity());
        pointService.exchangePoints(userId, reqDTO);
        return ApiResponse.success();
    }
}
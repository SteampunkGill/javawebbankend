// File: milktea-backend/src/main/java/com.milktea.app/controller/StoreV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.store.StoreDetailResDTO;
import com.milktea.app.dto.store.StoreNearbyReqDTO;
import com.milktea.app.dto.store.StoreNearbyResDTO;
import com.milktea.app.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/stores") // Base path for stores module
@RequiredArgsConstructor
@Slf4j
public class StoreV1Controller {

    private final StoreService storeService;

    // 方法1：使用@AuthenticationPrincipal注解
    private Long getUserId(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            // 从SecurityContextHolder获取认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
                // 如果认证信息存在且不是匿名用户
                Object authPrincipal = authentication.getPrincipal();
                if (authPrincipal instanceof User) {
                    return Long.parseLong(((User) authPrincipal).getUsername());
                } else if (authPrincipal instanceof String) {
                    // 如果principal是字符串形式的用户ID
                    return Long.parseLong((String) authPrincipal);
                }
            }
            throw new IllegalArgumentException("用户未认证");
        }
        return Long.parseLong(principal.getUsername());
    }

    // 方法2：直接从SecurityContextHolder获取用户ID
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("用户未认证");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return Long.parseLong(((User) principal).getUsername());
        } else if (principal instanceof String) {
            // 检查是否是匿名用户
            if ("anonymousUser".equals(principal)) {
                throw new IllegalArgumentException("用户未认证");
            }
            return Long.parseLong((String) principal);
        } else {
            throw new IllegalArgumentException("无法识别的principal类型");
        }
    }

    @GetMapping("/nearby") // Matches /stores/nearby
    public ApiResponse<StoreNearbyResDTO> getNearbyStores(@AuthenticationPrincipal User principal,
                                                          @Valid @ModelAttribute StoreNearbyReqDTO reqDTO) {
        // 详细的调试信息
        log.debug("=== Store Controller Debug ===");
        log.debug("Principal object: {}", principal);
        log.debug("Principal class: {}", principal != null ? principal.getClass().getName() : "null");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("SecurityContext Authentication: {}", auth);
        if (auth != null) {
            log.debug("Authentication principal: {}", auth.getPrincipal());
            log.debug("Principal class: {}", auth.getPrincipal().getClass().getName());
            log.debug("Authentication name: {}", auth.getName());
            log.debug("Authentication authorities: {}", auth.getAuthorities());
            log.debug("Authentication is authenticated: {}", auth.isAuthenticated());
        }
        log.debug("=== End Store Controller Debug ===");

        // 使用方法1
        Long userId = null;
        try {
            userId = getUserId(principal);
        } catch (IllegalArgumentException e) {
            // 如果用户未认证，userId为null，但接口仍然可以访问
            log.debug("User not authenticated, but /nearby endpoint is public");
        }

        log.info("Getting nearby stores for user {} at ({}, {}) radius {}", userId, reqDTO.getLatitude(), reqDTO.getLongitude(), reqDTO.getRadius());
        StoreNearbyResDTO resDTO = storeService.getNearbyStores(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/{storeId}") // Matches /stores/{id}
    public ApiResponse<StoreDetailResDTO> getStoreDetail(@AuthenticationPrincipal User principal,
                                                         @PathVariable("storeId") Long storeId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("Getting store detail for ID {} by user {}", storeId, userId);
        StoreDetailResDTO resDTO = storeService.getStoreDetail(userId, storeId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/{storeId}/favorite") // Matches /stores/{id}/favorite
    public ApiResponse<Void> addFavoriteStore(@AuthenticationPrincipal User principal,
                                              @PathVariable("storeId") Long storeId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("User {} adding store {} to favorites", userId, storeId);
        storeService.addFavoriteStore(userId, storeId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{storeId}/favorite") // Matches /stores/{id}/favorite
    public ApiResponse<Void> removeFavoriteStore(@AuthenticationPrincipal User principal,
                                                 @PathVariable("storeId") Long storeId) { // Renamed path variable for clarity
        Long userId = getUserId(principal);
        log.info("User {} removing store {} from favorites", userId, storeId);
        storeService.removeFavoriteStore(userId, storeId);
        return ApiResponse.success();
    }

    // 接口文档未明确提供获取门店收藏状态的接口，但通常会有。
    // 如果需要，可以添加一个 @GetMapping("/{storeId}/is-favorite")
}
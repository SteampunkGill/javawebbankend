package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.util.PaginationUtil;
import com.milktea.app.dto.user.*;
import com.milktea.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserV1Controller {

    private final UserService userService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        // 调试信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("=== Security Context Debug ===");
        log.debug("Authentication: {}", auth);
        log.debug("Principal: {}", principal);
        log.debug("Principal class: {}", principal != null ? principal.getClass().getName() : "null");
        log.debug("=== End Security Context Debug ===");

        if (principal == null) {
            throw new IllegalArgumentException("用户未认证");
        }

        return Long.parseLong(principal.getUsername());
    }
    @GetMapping("/profile")
    public ApiResponse<UserProfileResDTO> getUserProfile(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Getting user profile for user: {}", userId);
        UserProfileResDTO resDTO = userService.getUserProfile(userId);
        return ApiResponse.success(resDTO);
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResDTO> updateProfile(@AuthenticationPrincipal User principal,
                                                        @Valid @RequestBody UserProfileUpdateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Updating profile for user: {}", userId);
        UserProfileResDTO resDTO = userService.updateProfile(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PutMapping("/phone")
    public ApiResponse<Void> updatePhone(@AuthenticationPrincipal User principal,
                                         @Valid @RequestBody UserPhoneUpdateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Updating phone for user {} to: {}", userId, reqDTO.getPhone());
        userService.updatePhone(userId, reqDTO);
        return ApiResponse.success();
    }

    @GetMapping("/addresses")
    public ApiResponse<UserAddressResDTO> getUserAddresses(@AuthenticationPrincipal User principal,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getUserId(principal);
        log.info("Getting addresses for user: {}", userId);
        Pageable pageable = PaginationUtil.createPageable(page, limit);
        UserAddressResDTO resDTO = userService.getUserAddresses(userId, pageable);
        return ApiResponse.success(resDTO);
    }

    @GetMapping("/addresses/{addressId}")
    public ApiResponse<UserAddressResDTO.AddressDTO> getUserAddressDetail(@AuthenticationPrincipal User principal,
                                                                          @PathVariable Long addressId) {
        Long userId = getUserId(principal);
        log.info("Getting address detail {} for user {}", addressId, userId);
        UserAddressResDTO.AddressDTO resDTO = userService.getUserAddressDetail(userId, addressId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/addresses")
    public ApiResponse<UserAddressResDTO.AddressDTO> createUserAddress(@AuthenticationPrincipal User principal,
                                                                       @Valid @RequestBody UserAddressCreateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Creating address for user {}: {}", userId, reqDTO.getDetail());
        UserAddressResDTO.AddressDTO resDTO = userService.createUserAddress(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PutMapping("/addresses/{addressId}")
    public ApiResponse<UserAddressResDTO.AddressDTO> updateUserAddress(@AuthenticationPrincipal User principal,
                                                                       @PathVariable Long addressId,
                                                                       @Valid @RequestBody UserAddressCreateReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Updating address {} for user {}: {}", addressId, userId, reqDTO.getDetail());
        UserAddressResDTO.AddressDTO resDTO = userService.updateUserAddress(userId, addressId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ApiResponse<Void> deleteUserAddress(@AuthenticationPrincipal User principal,
                                               @PathVariable Long addressId) {
        Long userId = getUserId(principal);
        log.info("Deleting address {} for user {}", addressId, userId);
        userService.deleteUserAddress(userId, addressId);
        return ApiResponse.success();
    }

    @PutMapping("/addresses/{addressId}/default")
    public ApiResponse<Void> setDefaultUserAddress(@AuthenticationPrincipal User principal,
                                                   @PathVariable Long addressId) {
        Long userId = getUserId(principal);
        log.info("Setting address {} as default for user {}", addressId, userId);
        userService.setDefaultUserAddress(userId, addressId);
        return ApiResponse.success();
    }

    @GetMapping("/share-info")
    public ApiResponse<ShareInfoResDTO> getShareInfo(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Getting share info for user: {}", userId);
        ShareInfoResDTO resDTO = userService.getShareInfo(userId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/generate-poster")
    public ApiResponse<GeneratePosterResDTO> generateSharePoster(@AuthenticationPrincipal User principal,
                                                                 @Valid @RequestBody GeneratePosterReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Generating share poster for user: {}", userId);
        GeneratePosterResDTO resDTO = userService.generateSharePoster(userId, reqDTO);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/share-record")
    public ApiResponse<Void> recordShare(@AuthenticationPrincipal User principal,
                                         @Valid @RequestBody ShareRecordReqDTO reqDTO) {
        Long userId = getUserId(principal);
        log.info("Recording share for user {}: type={}, targetId={}, channel={}", userId, reqDTO.getType(), reqDTO.getTargetId(), reqDTO.getChannel());
        userService.recordShare(userId, reqDTO);
        return ApiResponse.success();
    }

    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(@RequestParam String phone,
                                                  @RequestParam String type) {
        log.info("Sending verification code to phone {} for type {}", phone, type);
        userService.sendVerificationCode(phone, type);
        return ApiResponse.success();
    }
}
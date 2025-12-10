// File: milktea-backend/src/main/java/com.milktea.app/controller/FileV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.file.FileUploadResDTO;
import com.milktea.app.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class FileV1Controller {

    private final FileService fileService;

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

    @PostMapping("/upload")
    public ApiResponse<FileUploadResDTO> uploadFile(@AuthenticationPrincipal User principal,
                                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                                    @RequestParam(value = "category", required = false) String category) {

        // 详细的调试信息
        log.debug("=== File Upload Controller Debug ===");
        log.debug("Principal: {}", principal);
        log.debug("Principal class: {}", principal != null ? principal.getClass().getName() : "null");
        log.debug("File is null: {}", file == null);
        if (file != null) {
            log.debug("File name: {}", file.getOriginalFilename());
            log.debug("File size: {}", file.getSize());
            log.debug("File content type: {}", file.getContentType());
            log.debug("File empty: {}", file.isEmpty());
        }
        log.debug("Category: {}", category);
        log.debug("=== End File Upload Controller Debug ===");

        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new BusinessException(ErrorCode.INVALID_PARAM, "文件不能为空");
        }

        Long userId = null;
        try {
            userId = getUserId(principal);
        } catch (IllegalArgumentException e) {
            // 如果用户未认证，userId为null
            log.debug("User not authenticated for file upload");
        }

        log.info("File upload request received from user {} for category: {}", userId, category);
        FileUploadResDTO resDTO = fileService.uploadFile(userId, file, category);
        return ApiResponse.success(resDTO);
    }
}
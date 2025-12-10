// File: milktea-backend/src/main/java/com.milktea.app/controller/MemberV1Controller.java
package com.milktea.app.controller;

import com.milktea.app.common.ApiResponse;
import com.milktea.app.dto.member.MemberInfoResDTO;
import com.milktea.app.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/member") // Base path for member module
@RequiredArgsConstructor
@Slf4j
public class MemberV1Controller {

    private final MemberService memberService;

    private Long getUserId(@AuthenticationPrincipal User principal) {
        return Long.parseLong(principal.getUsername());
    }

    @GetMapping("/info") // Matches /member/info
    public ApiResponse<MemberInfoResDTO> getMemberInfo(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("Getting member info for user: {}", userId);
        MemberInfoResDTO resDTO = memberService.getMemberInfo(userId);
        return ApiResponse.success(resDTO);
    }

    @PostMapping("/birthdaygift/receive") // Matches /member/birthdaygift/receive
    public ApiResponse<Void> receiveBirthdayGift(@AuthenticationPrincipal User principal) {
        Long userId = getUserId(principal);
        log.info("User {} attempting to receive birthday gift", userId);
        memberService.receiveBirthdayGift(userId);
        return ApiResponse.success();
    }
}
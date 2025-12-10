package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.auth.AccountLoginReqDTO;
import com.milktea.app.dto.auth.UserAuthResDTO;
import com.milktea.app.dto.auth.WechatLoginReqDTO;
import com.milktea.app.dto.user.UserProfileResDTO;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.jwt.JwtService;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.AuthService;
import com.milktea.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService; // To leverage user profile mapping
    private final RestTemplate restTemplate; // Placeholder for external API calls

    @Override
    @Transactional
    public UserAuthResDTO wechatLogin(WechatLoginReqDTO reqDTO) {
        // 这是微信实现的: Placeholder for WeChat API call
        // Example: Call WeChat API to exchange code for openid and session_key
        String wechatOpenId = mockWechatAuth(reqDTO.getCode());
        if (wechatOpenId == null) {
            throw new BusinessException(ErrorCode.WECHAT_LOGIN_FAILED, "WeChat authentication failed.");
        }

        Optional<UserEntity> existingUser = userRepository.findByWechatOpenid(wechatOpenId);
        UserEntity user;

        if (existingUser.isEmpty()) {
            // New user registration
            user = new UserEntity();
            user.setWechatOpenid(wechatOpenId);
            user.setNickname(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getNickName() : "微信用户");
            user.setAvatarUrl(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getAvatarUrl() : null);
            user.setGender(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getGender().shortValue() : (short)0);
            user.setCountry(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getCountry() : null);
            user.setProvince(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getProvince() : null);
            user.setCity(reqDTO.getUserInfo() != null ? reqDTO.getUserInfo().getCity() : null);
            user.setIsActive(true);
            user.setGrowthValue(0);
            user.setPoints(0);
            user.setBalance(java.math.BigDecimal.ZERO);
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user = userRepository.save(user);
            log.info("New user registered via WeChat: {}", user.getId());
        } else {
            user = existingUser.get();
            // Update last login time and potentially user info if provided
            user.setLastLoginAt(Instant.now());
            if (reqDTO.getUserInfo() != null) {
                user.setNickname(reqDTO.getUserInfo().getNickName());
                user.setAvatarUrl(reqDTO.getUserInfo().getAvatarUrl());
                user.setGender(reqDTO.getUserInfo().getGender().shortValue());
                user.setCountry(reqDTO.getUserInfo().getCountry());
                user.setProvince(reqDTO.getUserInfo().getProvince());
                user.setCity(reqDTO.getUserInfo().getCity());
            }
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getId());
        return buildUserAuthResDTO(user, token);
    }

    @Override
    @Transactional
    public UserAuthResDTO accountLogin(AccountLoginReqDTO reqDTO) {
        Optional<UserEntity> userOptional = userRepository.findByUsernameOrPhoneOrEmail(
                reqDTO.getUsername(), reqDTO.getUsername(), reqDTO.getUsername());

        if (userOptional.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        UserEntity user = userOptional.get();

        // Placeholder for captcha validation
        if (reqDTO.getCaptcha() != null && !validateCaptcha(reqDTO.getCaptcha())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE, "验证码错误");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(reqDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码不正确");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId());
        return buildUserAuthResDTO(user, token);
    }

    @Override
    public void logout(String token) {
        // 直接调用 JwtService 的 addToBlacklist 方法
        try {
            jwtService.addToBlacklist(token);
            log.info("User logged out, token added to blacklist");
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    private UserAuthResDTO buildUserAuthResDTO(UserEntity user, String token) {
        UserProfileResDTO.UserDetailDTO userDetail = new UserProfileResDTO.UserDetailDTO();
        userDetail.setId(String.valueOf(user.getId()));
        userDetail.setNickname(user.getNickname());
        userDetail.setAvatar(user.getAvatarUrl());
        userDetail.setPhone(user.getPhone());
        // For level, assuming a direct mapping or simple logic
        userDetail.setLevel(mapMemberLevelToCode(user.getMemberLevel() != null ? user.getMemberLevel().getName() : null));
        userDetail.setLevelName(user.getMemberLevel() != null ? user.getMemberLevel().getName() : "普通会员");
        userDetail.setPoints(user.getPoints());
        userDetail.setBalance(user.getBalance());
        userDetail.setBirthday(user.getBirthday());
        userDetail.setCreatedAt(user.getCreatedAt());

        return new UserAuthResDTO(token, userDetail);
    }

    private String mockWechatAuth(String code) {
        // In a real application, this would call WeChat's API to get openid
        log.info("Mocking WeChat auth for code: {}", code);
        if ("mock_success_code".equals(code)) {
            // Simulate successful auth for a new user, or a known user
            return "mock_openid_" + System.currentTimeMillis(); // Generate a unique mock openid
        }
        if ("mock_existing_code".equals(code)) {
            return "mock_openid_existing_user"; // Return a consistent openid for an "existing" mock user
        }
        // Simulate failure
        return null;
    }

    private boolean validateCaptcha(String captcha) {
        // Placeholder for actual captcha (e.g., SMS verification code) validation logic
        // In a real scenario, this would involve checking against a stored code and its expiration.
        log.info("Mocking captcha validation for: {}", captcha);
        return "123456".equals(captcha); // Simple mock check for '123456'
    }

    private String mapMemberLevelToCode(String levelName) {
        if (levelName == null) {
            return "normal";
        }
        return switch (levelName) {
            case "普通会员" -> "normal";
            case "黄金会员" -> "gold";
            case "钻石会员" -> "diamond";
            default -> "normal"; // Default or unknown level
        };
    }
}
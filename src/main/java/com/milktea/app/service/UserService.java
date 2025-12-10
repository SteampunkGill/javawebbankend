// File: milktea-backend/src/main/java/com.milktea.app/service/UserService.java
package com.milktea.app.service;

import com.milktea.app.dto.user.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface UserService {
    UserProfileResDTO getUserProfile(Long userId);
    UserProfileResDTO updateProfile(Long userId, UserProfileUpdateReqDTO reqDTO);
    void updatePhone(Long userId, UserPhoneUpdateReqDTO reqDTO);
    UserAddressResDTO getUserAddresses(Long userId, Pageable pageable);
    UserAddressResDTO.AddressDTO getUserAddressDetail(Long userId, Long addressId);
    UserAddressResDTO.AddressDTO createUserAddress(Long userId, UserAddressCreateReqDTO reqDTO);
    UserAddressResDTO.AddressDTO updateUserAddress(Long userId, Long addressId, UserAddressCreateReqDTO reqDTO);
    void deleteUserAddress(Long userId, Long addressId);
    void setDefaultUserAddress(Long userId, Long addressId);
    // New: 1.3.6 根据定位获取地址
    UserAddressResDTO getAddressesByLocation(Long userId, BigDecimal longitude, BigDecimal latitude, Integer radius, Integer limit);
    ShareInfoResDTO getShareInfo(Long userId);
    GeneratePosterResDTO generateSharePoster(Long userId, GeneratePosterReqDTO reqDTO); // 这是微信实现的
    void recordShare(Long userId, ShareRecordReqDTO reqDTO); // 这是微信实现的
    // Add methods for sending verification codes (SMS service placeholder)
    void sendVerificationCode(String phone, String type);
    void verifyVerificationCode(String phone, String code, String type);
}
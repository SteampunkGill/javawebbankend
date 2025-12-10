// File: milktea-backend/src/main/java/com.milktea.app.service/impl/UserServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.common.util.DateUtil;
import com.milktea.app.common.util.GeoUtil;
import com.milktea.app.dto.home.HomePageResDTO; // For mapToNearbyStoreDTO
import com.milktea.app.dto.store.StoreNearbyReqDTO; // For getAddressesByLocation
import com.milktea.app.dto.user.*;
import com.milktea.app.entity.MemberLevelEntity;
import com.milktea.app.entity.StoreEntity;
import com.milktea.app.entity.StoreImageEntity; // Added for mapToNearbyStoreDTO
import com.milktea.app.entity.StoreServiceEntity; // Added for mapToNearbyStoreDTO
import com.milktea.app.entity.StoreTagEntity; // Added for mapToNearbyStoreDTO
import com.milktea.app.entity.UserAddressEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.entity.UserFavoriteStoreEntity; // For mapToNearbyStoreDTO
import com.milktea.app.entity.VerificationCodeEntity;
import com.milktea.app.entity.UserShareEntity; // 添加这个导入
import com.milktea.app.repository.MemberLevelRepository;
import com.milktea.app.repository.StoreRepository; // For mapToNearbyStoreDTO
import com.milktea.app.repository.UserAddressRepository;
import com.milktea.app.repository.UserCouponRepository;
import com.milktea.app.repository.UserFavoriteStoreRepository; // For mapToNearbyStoreDTO
import com.milktea.app.repository.UserRepository;
import com.milktea.app.repository.UserShareRepository;
import com.milktea.app.repository.VerificationCodeRepository;
import com.milktea.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder; // If password reset/change is involved
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserShareRepository userShareRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder; // For password related actions
    private final StoreRepository storeRepository; // For mapToNearbyStoreDTO
    private final UserFavoriteStoreRepository userFavoriteStoreRepository; // For mapToNearbyStoreDTO

    @Override
    @Transactional(readOnly = true)
    public UserProfileResDTO getUserProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        UserProfileResDTO resDTO = new UserProfileResDTO();
        resDTO.setId(user.getId());
        resDTO.setNickname(user.getNickname());
        resDTO.setAvatar(user.getAvatarUrl());
        resDTO.setPhone(user.getPhone());
        resDTO.setEmail(user.getEmail());
        resDTO.setGender(user.getGender() != null ? user.getGender().intValue() : 0);
        resDTO.setBirthday(user.getBirthday());
        resDTO.setPoints(user.getPoints());
        resDTO.setBalance(user.getBalance());
        resDTO.setGrowthValue(user.getGrowthValue());
        resDTO.setCreatedAt(user.getCreatedAt());

        // Member Level Info
        MemberLevelEntity currentLevel = user.getMemberLevel();
        if (currentLevel != null) {
            resDTO.setLevel(currentLevel.getName()); // Use name as code for DTO simplicity
            resDTO.setLevelName(currentLevel.getName());
        } else {
            resDTO.setLevel("normal");
            resDTO.setLevelName("普通会员");
        }

        Optional<MemberLevelEntity> nextLevelOptional = memberLevelRepository.findFirstByMinGrowthValueGreaterThanOrderByMinGrowthValueAsc(user.getGrowthValue());
        if (nextLevelOptional.isPresent()) {
            resDTO.setNextLevelPoints(nextLevelOptional.get().getMinGrowthValue() - user.getGrowthValue());
        } else {
            resDTO.setNextLevelPoints(0); // Already max level
        }

        // Computed counts
        resDTO.setCouponCount((int) userCouponRepository.countByUserIdAndStatus(userId, "available"));
        resDTO.setUnreadMessageCount((int) 0L); // Placeholder for actual unread message count

        // Member Card
        UserProfileResDTO.MemberCardDTO memberCardDTO = new UserProfileResDTO.MemberCardDTO();
        memberCardDTO.setCardNo(user.getMemberCardNo());
        memberCardDTO.setStatus(user.getMemberCardStatus());
        memberCardDTO.setExpireDate(user.getMemberCardExpireDate());
        resDTO.setMemberCard(memberCardDTO);

        return resDTO;
    }

    @Override
    @Transactional
    public UserProfileResDTO updateProfile(Long userId, UserProfileUpdateReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        user.setNickname(reqDTO.getNickname());
        user.setAvatarUrl(reqDTO.getAvatar());
        user.setGender(reqDTO.getGender() != null ? reqDTO.getGender().shortValue() : null);
        user.setBirthday(reqDTO.getBirthday());
        if (reqDTO.getEmail() != null && !reqDTO.getEmail().equals(user.getEmail())) {
            // Check for email uniqueness if changing
            if (userRepository.findByEmail(reqDTO.getEmail()).isPresent()) {
                throw new BusinessException(ErrorCode.CONFLICT, "Email is already in use.");
            }
            user.setEmail(reqDTO.getEmail());
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return getUserProfile(userId); // Return updated profile
    }

    @Override
    @Transactional
    public void updatePhone(Long userId, UserPhoneUpdateReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        // Verify the captcha
        verifyVerificationCode(reqDTO.getPhone(), reqDTO.getCaptcha(), "update_phone");

        // Check if new phone is already bound to another user
        Optional<UserEntity> existingUserWithPhone = userRepository.findByPhone(reqDTO.getPhone());
        if (existingUserWithPhone.isPresent() && !existingUserWithPhone.get().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_BOUND, "Phone number is already bound to another account.");
        }

        user.setPhone(reqDTO.getPhone());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        log.info("User {} updated phone to {}", userId, reqDTO.getPhone());
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressResDTO getUserAddresses(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        List<UserAddressEntity> addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);

        // Manual pagination for simplicity as findByUserId returns all.
        // In a real app, the repository method would return a Page<UserAddressEntity>.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), addresses.size());
        List<UserAddressEntity> pagedAddresses = addresses.subList(start, end);


        List<UserAddressResDTO.AddressDTO> addressDTOs = pagedAddresses.stream()
                .map(this::mapToAddressDTO)
                .collect(Collectors.toList());

        UserAddressResDTO resDTO = new UserAddressResDTO();
        resDTO.setList(addressDTOs);
        resDTO.setTotal(addresses.size());
        resDTO.setPage(pageable.getPageNumber() + 1);
        resDTO.setLimit(pageable.getPageSize());
        return resDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressResDTO.AddressDTO getUserAddressDetail(Long userId, Long addressId) {
        UserAddressEntity address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found."));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to address.");
        }
        return mapToAddressDTO(address);
    }

    @Override
    @Transactional
    public UserAddressResDTO.AddressDTO createUserAddress(Long userId, UserAddressCreateReqDTO reqDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        if (reqDTO.getIsDefault() != null && reqDTO.getIsDefault()) {
            // Unset previous default address for this user
            userAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(oldDefault -> {
                        oldDefault.setIsDefault(false);
                        userAddressRepository.save(oldDefault);
                    });
        } else {
            // If no default specified, and it's the first address, make it default
            // 修改这里：使用 findByUserIdOrderByIsDefaultDescCreatedAtDesc 替代 findByUserId
            List<UserAddressEntity> userAddresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (userAddresses.isEmpty()) {
                reqDTO.setIsDefault(true);
            }
        }


        UserAddressEntity address = new UserAddressEntity();
        address.setUser(user);
        address.setName(reqDTO.getName());
        address.setPhone(reqDTO.getPhone());
        address.setProvince(reqDTO.getProvince());
        address.setCity(reqDTO.getCity());
        address.setDistrict(reqDTO.getDistrict());
        address.setDetail(reqDTO.getDetail());
        address.setPostalCode(reqDTO.getPostalCode());
        address.setIsDefault(reqDTO.getIsDefault());
        address.setType(reqDTO.getType());
        address.setLabel(reqDTO.getLabel());
        address.setLongitude(reqDTO.getLongitude());
        address.setLatitude(reqDTO.getLatitude());
        address.setCreatedAt(Instant.now());
        address.setUpdatedAt(Instant.now());

        address = userAddressRepository.save(address);
        log.info("User {} created new address {}", userId, address.getId());
        return mapToAddressDTO(address);
    }

    @Override
    @Transactional
    public UserAddressResDTO.AddressDTO updateUserAddress(Long userId, Long addressId, UserAddressCreateReqDTO reqDTO) {
        UserAddressEntity address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found."));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to address.");
        }

        if (reqDTO.getIsDefault() != null && reqDTO.getIsDefault() && !address.getIsDefault()) {
            // If setting to default, unset previous default for this user
            userAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(oldDefault -> {
                        oldDefault.setIsDefault(false);
                        userAddressRepository.save(oldDefault);
                    });
        } else if (reqDTO.getIsDefault() != null && !reqDTO.getIsDefault() && address.getIsDefault()) {
            // If unsetting default, ensure there's at least one default left or prevent if it's the only one
            // 修改这里：使用 findByUserIdOrderByIsDefaultDescCreatedAtDesc 然后计算大小
            List<UserAddressEntity> userAddresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (userAddresses.size() == 1) { // 只有一个地址，就是当前这个
                throw new BusinessException(ErrorCode.DEFAULT_ADDRESS_CANNOT_DELETE, "Cannot unset the only address as default.");
            }
        }

        address.setName(reqDTO.getName());
        address.setPhone(reqDTO.getPhone());
        address.setProvince(reqDTO.getProvince());
        address.setCity(reqDTO.getCity());
        address.setDistrict(reqDTO.getDistrict());
        address.setDetail(reqDTO.getDetail());
        address.setPostalCode(reqDTO.getPostalCode());
        address.setIsDefault(reqDTO.getIsDefault());
        address.setType(reqDTO.getType());
        address.setLabel(reqDTO.getLabel());
        address.setLongitude(reqDTO.getLongitude());
        address.setLatitude(reqDTO.getLatitude());
        address.setUpdatedAt(Instant.now());

        address = userAddressRepository.save(address);
        log.info("User {} updated address {}", userId, address.getId());
        return mapToAddressDTO(address);
    }

    @Override
    @Transactional
    public void deleteUserAddress(Long userId, Long addressId) {
        UserAddressEntity address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found."));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to address.");
        }

        if (address.getIsDefault()) {
            // If deleting default, reassign default to another address if exists
            List<UserAddressEntity> otherAddresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (otherAddresses.size() > 1) {
                UserAddressEntity newDefault = otherAddresses.stream()
                        .filter(a -> !a.getId().equals(addressId))
                        .findFirst() // Pick the first available non-default as new default
                        .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "Could not assign new default address."));
                newDefault.setIsDefault(true);
                userAddressRepository.save(newDefault);
            } else if (otherAddresses.size() == 1) { // Only one address is left, it's the default being deleted
                // No other address to assign default to, which is fine if user has no addresses left.
            }
        }
        userAddressRepository.delete(address);
        log.info("User {} deleted address {}", userId, address.getId());
    }

    @Override
    @Transactional
    public void setDefaultUserAddress(Long userId, Long addressId) {
        UserAddressEntity addressToSetDefault = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found."));

        if (!addressToSetDefault.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to address.");
        }

        if (!addressToSetDefault.getIsDefault()) {
            // Unset previous default address for this user
            userAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(oldDefault -> {
                        oldDefault.setIsDefault(false);
                        userAddressRepository.save(oldDefault);
                    });
            // Set new default
            addressToSetDefault.setIsDefault(true);
            userAddressRepository.save(addressToSetDefault);
            log.info("User {} set address {} as default", userId, addressId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressResDTO getAddressesByLocation(Long userId, BigDecimal longitude, BigDecimal latitude, Integer radius, Integer limit) {
        // Find stores near the given location
        List<StoreEntity> nearbyStores = storeRepository.findNearbyStoresNative(latitude, longitude, radius.doubleValue(), limit);

        List<UserAddressResDTO.AddressDTO> addressDTOs = new ArrayList<>();
        // Add a "current location" mock address
        addressDTOs.add(new UserAddressResDTO.AddressDTO(
                null, "当前位置", null, null, null, null, "当前位置", null,
                true, "current", "当前位置", longitude, latitude, Instant.now()
        ));

        // Convert nearby stores to address DTOs, potentially adding them as "suggested addresses"
        nearbyStores.stream()
                .map(store -> new UserAddressResDTO.AddressDTO(
                        null, store.getName(), store.getPhone(),
                        store.getAddress().split(" ")[0], // Simplified province/city
                        store.getAddress().split(" ")[1],
                        store.getAddress().split(" ").length > 2 ? store.getAddress().split(" ")[2] : null,
                        store.getAddress(), null, false, "store", store.getName(),
                        store.getLongitude(), store.getLatitude(), Instant.now()
                ))
                .forEach(addressDTOs::add);

        // For a full implementation, you'd integrate with a real geo-coding service to get address from lat/lon.
        // The DTO format requires a list, total, page, limit.
        return new UserAddressResDTO(addressDTOs, addressDTOs.size(), 1, addressDTOs.size());
    }


    @Override
    @Transactional(readOnly = true)
    public ShareInfoResDTO getShareInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));
        ShareInfoResDTO resDTO = new ShareInfoResDTO();
        resDTO.setTitle("邀请好友喝奶茶");
        resDTO.setDescription("邀请好友注册,各得20元优惠券");
        resDTO.setImage("https://example.com/share_image.jpg"); // Placeholder
        resDTO.setInviteCode("INVITE" + String.format("%04d", user.getId())); // Simple invite code based on user ID
        resDTO.setPath("/pages/invite/invite?code=" + resDTO.getInviteCode());
        resDTO.setInviteCount((int) userShareRepository.countByUserIdAndType(userId, "invite"));
        resDTO.setRewardPoints(100); // Placeholder
        resDTO.setRewardCoupon(new ShareInfoResDTO.RewardCouponDTO("coupon_id_123", "20元优惠券", BigDecimal.valueOf(20.00)));

        return resDTO;
    }

    @Override
    public GeneratePosterResDTO generateSharePoster(Long userId, GeneratePosterReqDTO reqDTO) {
        // 这是微信实现的: This typically involves calling a WeChat API or a backend image generation service
        // that integrates with WeChat to generate posters with QR codes pointing to mini-program paths.
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        // Placeholder for image generation service
        log.info("Generating share poster for user {} with QR content {}", userId, reqDTO.getQrCodeContent());
        String mockPosterUrl = "https://example.com/generated_poster_" + UUID.randomUUID().toString() + ".png";
        Instant expireAt = Instant.now().plus(7, ChronoUnit.DAYS); // Poster valid for 7 days

        return new GeneratePosterResDTO(mockPosterUrl, expireAt);
    }

    @Override
    @Transactional
    public void recordShare(Long userId, ShareRecordReqDTO reqDTO) {
        // 这是微信实现的: This often corresponds to client-side sharing events reported back to the backend.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        UserShareEntity share = new UserShareEntity();
        share.setUser(user);
        share.setType(reqDTO.getType());
        share.setTargetId(reqDTO.getTargetId());
        share.setChannel(reqDTO.getChannel());
        if ("invite".equals(reqDTO.getType())) {
            share.setInviteCode("INVITE" + String.format("%04d", user.getId())); // Ensure invite code is consistent
        }
        share.setCreatedAt(Instant.now());
        userShareRepository.save(share);
        log.info("User {} recorded share of type {} on channel {}", userId, reqDTO.getType(), reqDTO.getChannel());

        // Further logic for rewards (e.g., if it's an invite and the invitee registers)
    }

    @Override
    @Transactional
    public void sendVerificationCode(String phone, String type) {
        // Placeholder for SMS sending service integration
        log.info("Sending verification code to phone {} for type {}", phone, type);
        // Generate a 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));

        // Invalidate any previous unused codes of the same type for this phone
        verificationCodeRepository.findTopByPhoneAndTypeAndExpiresAtAfterAndIsUsedFalseOrderBySentAtDesc(phone, type, Instant.now())
                .ifPresent(prevCode -> {
                    prevCode.setIsUsed(true); // Mark as used/invalidated
                    verificationCodeRepository.save(prevCode);
                });

        VerificationCodeEntity verificationCode = new VerificationCodeEntity();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setSentAt(Instant.now());
        verificationCode.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES)); // Expires in 5 minutes
        verificationCode.setIsUsed(false);
        verificationCode.setCreatedAt(Instant.now());
        verificationCodeRepository.save(verificationCode);

        // Here you would call an actual SMS gateway service
        log.info("Mock SMS sent: Phone={}, Code={}", phone, code);
    }

    @Override
    @Transactional
    public void verifyVerificationCode(String phone, String inputCode, String type) {
        VerificationCodeEntity latestCode = verificationCodeRepository.findTopByPhoneAndTypeAndExpiresAtAfterAndIsUsedFalseOrderBySentAtDesc(
                phone, type, Instant.now()
        ).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE, "Verification code not found or expired."));

        if (!latestCode.getCode().equals(inputCode)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE, "Verification code is incorrect.");
        }

        // Mark code as used
        latestCode.setIsUsed(true);
        verificationCodeRepository.save(latestCode);
        log.info("Verification code verified successfully for phone {}", phone);
    }

    private UserAddressResDTO.AddressDTO mapToAddressDTO(UserAddressEntity entity) {
        UserAddressResDTO.AddressDTO dto = new UserAddressResDTO.AddressDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setDetail(entity.getDetail());
        dto.setPostalCode(entity.getPostalCode());
        dto.setIsDefault(entity.getIsDefault());
        dto.setType(entity.getType());
        dto.setLabel(entity.getLabel());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private HomePageResDTO.NearbyStoreDTO mapToNearbyStoreDTO(StoreEntity entity, BigDecimal userLat, BigDecimal userLon, Long userId) {
        HomePageResDTO.NearbyStoreDTO dto = new HomePageResDTO.NearbyStoreDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());

        double distance = GeoUtil.calculateDistance(
                userLat.doubleValue(), userLon.doubleValue(),
                entity.getLatitude().doubleValue(), entity.getLongitude().doubleValue()
        );
        dto.setDistance((int) Math.round(distance));

        dto.setBusinessHours(entity.getBusinessHours());
        dto.setStatus(entity.getStatus());
        dto.setPhone(entity.getPhone());
        dto.setServices(entity.getServices().stream().map(StoreServiceEntity::getServiceType).collect(Collectors.toList()));
        dto.setTags(entity.getTags());
        dto.setDeliveryFee(entity.getDeliveryFee());
        dto.setMinimumOrderAmount(entity.getMinimumOrderAmount());
        dto.setRating(entity.getRating());
        // For images, ensure it's loaded from StoreImageEntity
        dto.setImages(entity.getImages() != null ? entity.getImages().stream().map(StoreImageEntity::getImageUrl).collect(Collectors.toList()) : Collections.emptyList());
        dto.setCurrentWaitTime(entity.getCurrentWaitTime());
        // This 'isFavorite' cannot be directly determined without knowing the requesting user's favorites
        dto.setIsFavorite(userId != null && userFavoriteStoreRepository.existsByUserIdAndStoreId(userId, entity.getId()));
        dto.setLongitude(entity.getLongitude()); // Added from StoreDetailResDTO
        dto.setLatitude(entity.getLatitude());   // Added from StoreDetailResDTO
        return dto;
    }
}
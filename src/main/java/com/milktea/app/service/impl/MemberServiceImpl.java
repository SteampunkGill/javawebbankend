// File: milktea-backend/src/main/java/com.milktea.app/service/impl/MemberServiceImpl.java
package com.milktea.app.service.impl;

import com.milktea.app.common.constant.ErrorCode;
import com.milktea.app.common.exception.BusinessException;
import com.milktea.app.dto.member.MemberInfoResDTO;
import com.milktea.app.entity.MemberLevelEntity;
import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.MemberLevelRepository;
import com.milktea.app.repository.UserRepository;
import com.milktea.app.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final MemberLevelRepository memberLevelRepository;
    private final ObjectMapper objectMapper; // To parse privileges_json

    @Override
    @Transactional(readOnly = true)
    public MemberInfoResDTO getMemberInfo(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        MemberInfoResDTO resDTO = new MemberInfoResDTO();
        resDTO.setGrowthValue(user.getGrowthValue());

        // Current member level
        MemberLevelEntity currentLevel = user.getMemberLevel();
        if (currentLevel != null) {
            resDTO.setLevel(currentLevel.getName()); // Use name as code for now
            resDTO.setLevelName(currentLevel.getName());
            // Parse privileges
            if (currentLevel.getPrivilegesJson() != null) {
                try {
                    List<MemberInfoResDTO.PrivilegeDTO> privileges = objectMapper.readValue(
                            currentLevel.getPrivilegesJson(),
                            new TypeReference<List<MemberInfoResDTO.PrivilegeDTO>>() {}
                    );
                    resDTO.setPrivileges(privileges);
                } catch (Exception e) {
                    log.error("Failed to parse privileges JSON for level {}: {}", currentLevel.getId(), e.getMessage());
                    resDTO.setPrivileges(new ArrayList<>());
                }
            } else {
                resDTO.setPrivileges(new ArrayList<>());
            }
        } else {
            // Default to a basic/normal level if no level is assigned
            resDTO.setLevel("normal");
            resDTO.setLevelName("普通会员");
            resDTO.setPrivileges(new ArrayList<>());
        }

        // Next member level calculation
        Optional<MemberLevelEntity> nextLevelOptional = memberLevelRepository.findFirstByMinGrowthValueGreaterThanOrderByMinGrowthValueAsc(user.getGrowthValue());
        if (nextLevelOptional.isPresent()) {
            resDTO.setNextLevel(nextLevelOptional.get().getName());
            resDTO.setNextLevelName(nextLevelOptional.get().getName());
            resDTO.setNeedGrowthValue(nextLevelOptional.get().getMinGrowthValue() - user.getGrowthValue());
        } else {
            resDTO.setNextLevel(null);
            resDTO.setNextLevelName("已是最高等级");
            resDTO.setNeedGrowthValue(0);
        }

        // Member card expiration
        resDTO.setExpireDate(user.getMemberCardExpireDate());

        // Birthday gift (placeholder logic)
        // Check if current month is user's birthday month
        boolean isBirthdayMonth = user.getBirthday() != null && user.getBirthday().getMonth() == LocalDate.now().getMonth();
        MemberInfoResDTO.BirthdayGiftDTO birthdayGiftDTO = new MemberInfoResDTO.BirthdayGiftDTO();
        birthdayGiftDTO.setAvailable(isBirthdayMonth);
        if (isBirthdayMonth) {
            List<MemberInfoResDTO.GiftItemDTO> gifts = new ArrayList<>();
            gifts.add(new MemberInfoResDTO.GiftItemDTO("coupon", "生日专属优惠券", "免费饮品一杯"));
            birthdayGiftDTO.setGifts(gifts);
            birthdayGiftDTO.setExpireDate(LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1)); // Expires end of current month
        } else {
            birthdayGiftDTO.setGifts(new ArrayList<>());
            birthdayGiftDTO.setExpireDate(null);
        }
        resDTO.setBirthdayGift(birthdayGiftDTO);

        return resDTO;
    }

    @Override
    @Transactional
    public void receiveBirthdayGift(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST, "User not found."));

        // Placeholder for checking if user is eligible for birthday gift and has not received it yet
        boolean isBirthdayMonth = user.getBirthday() != null && user.getBirthday().getMonth() == LocalDate.now().getMonth();
        // Additional check: user should not have already received the gift for the current year.
        // This would require a "birthday_gift_received_year" field on UserEntity or similar.
        boolean alreadyReceivedThisYear = false; // Mocking false

        if (!isBirthdayMonth) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Not your birthday month.");
        }
        if (alreadyReceivedThisYear) {
            throw new BusinessException(ErrorCode.CONFLICT, "Birthday gift already received this year.");
        }

        // Logic to issue coupon or other gifts
        // Example: couponService.receiveBirthdayCoupon(userId, "birthday_coupon_template_id");
        log.info("User {} received birthday gift (mock).", userId);

        // Update user to mark birthday gift as received for this year
        // user.setLastBirthdayGiftReceivedYear(LocalDate.now().getYear());
        // userRepository.save(user);
    }
}
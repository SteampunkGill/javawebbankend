// File: milktea-backend/src/main/java/com.milktea.app/service/MemberService.java
package com.milktea.app.service;

import com.milktea.app.dto.member.MemberInfoResDTO;

public interface MemberService {
    MemberInfoResDTO getMemberInfo(Long userId);
    // New: 5.3.2 领取生日特权
    void receiveBirthdayGift(Long userId);
}
// File: milktea-backend/src/main/java/com.milktea.app/repository/UserRepository.java
package com.milktea.app.repository;

import com.milktea.app.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByWechatOpenid(String wechatOpenid);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsernameOrPhoneOrEmail(String username, String phone, String email);

        boolean existsByPhone(String phone);

}
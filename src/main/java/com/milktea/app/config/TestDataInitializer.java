package com.milktea.app.config;

import com.milktea.app.entity.UserEntity;
import com.milktea.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
@Slf4j
@RequiredArgsConstructor
@Profile({"dev", "test"}) // 只在开发或测试环境生效
public class TestDataInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Environment environment;

    @Bean
    public CommandLineRunner initTestUsers() {
        return args -> {
            // 1. 打印当前激活的环境
            String[] activeProfiles = environment.getActiveProfiles();
            log.info("当前激活的环境: {}", Arrays.toString(activeProfiles));
            log.info("是否包含dev环境: {}", Arrays.asList(activeProfiles).contains("dev"));

            // 2. 检查数据库连接
            try {
                long userCount = userRepository.count();
                log.info("当前数据库中的用户数量: {}", userCount);
            } catch (Exception e) {
                log.error("无法连接到数据库: {}", e.getMessage());
                return;
            }

            // 3. 开始创建测试用户
            log.info("开始初始化测试用户...");

            // 创建测试用户 - 使用不同的手机号避免冲突
            createTestUser("admin", "admin@milktea.com", "Admin@2024", "管理员", "13800138001");
            createTestUser("testuser", "test@milktea.com", "Test123456", "测试用户", "13800138002");
            createTestUser("vipuser", "vip@milktea.com", "Vip123456", "VIP用户", "13800138003");

            // 4. 验证创建结果
            long finalUserCount = userRepository.count();
            log.info("创建后数据库中的用户数量: {}", finalUserCount);

            // 5. 列出所有用户
            userRepository.findAll().forEach(user -> {
                log.info("用户: {}, 邮箱: {}, 手机: {}, 创建时间: {}",
                        user.getUsername(), user.getEmail(), user.getPhone(), user.getCreatedAt());
            });

            log.info("测试用户初始化完成！");
        };
    }

    private void createTestUser(String username, String email, String password,
                                String nickname, String phone) {
        try {
            // 检查用户是否已存在（通过用户名或手机号）
            boolean existsByUsername = userRepository.findByUsername(username).isPresent();
            boolean existsByPhone = userRepository.findByPhone(phone).isPresent();

            if (existsByUsername || existsByPhone) {
                log.info("⏭️ 用户 {} (手机: {}) 已存在，跳过创建", username, phone);
                return;
            }

            log.info("开始创建用户: {}, 手机: {}", username, phone);

            UserEntity user = new UserEntity();

            // 设置基本字段
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setNickname(nickname);
            user.setPhone(phone);

            // 设置可选字段
            user.setAvatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + username);
            user.setGender((short) 0);
            user.setCountry("中国");
            user.setProvince("广东");
            user.setCity("深圳");
            user.setBirthday(LocalDate.of(1990, 1, 1));

            // 设置会员相关字段
            user.setGrowthValue(100);
            user.setPoints(500);
            user.setBalance(new BigDecimal("100.00"));
            user.setMemberCardNo("MC" + System.currentTimeMillis());
            user.setMemberCardStatus("active");
            user.setMemberCardExpireDate(LocalDate.now().plusYears(1));

            // 设置时间字段
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setLastLoginAt(Instant.now());

            // 设置状态
            user.setIsActive(true);

            // 保存用户
            UserEntity savedUser = userRepository.save(user);
            log.info("✅ 成功创建用户: {}, ID: {}, 手机: {}", username, savedUser.getId(), phone);

            // 输出密码哈希
            log.info("密码: {}, 哈希值: {}", password, savedUser.getPasswordHash());
        } catch (Exception e) {
            log.error("❌ 创建用户 {} (手机: {}) 失败: {}", username, phone, e.getMessage(), e);
        }
    }
}
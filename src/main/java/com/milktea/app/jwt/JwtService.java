package com.milktea.app.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    // 使用 Set 存储黑名单 token
    private final Set<String> tokenBlacklist = new HashSet<>();

    // 添加同步锁确保线程安全
    private final Object lock = new Object();

    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        // 使用 0.9.1 版本的 API
        return Jwts.builder()
                .setClaims(claims)  // 0.9.1 版本使用 setClaims()
                .setSubject(String.valueOf(userId))  // 0.9.1 版本使用 setSubject()
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 0.9.1 版本使用 setIssuedAt()
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000L))  // 0.9.1 版本使用 setExpiration()
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret().getBytes())  // 0.9.1 版本使用 signWith(SignatureAlgorithm, byte[])
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // 检查是否在黑名单中
            if (isTokenBlacklisted(token)) {
                log.warn("Token在黑名单中: {}", token);
                return false;
            }

            // 0.9.1 版本：Jwts.parser() 返回 JwtParser，不是 JwtParserBuilder
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret().getBytes())  // 0.9.1 版本使用 setSigningKey(byte[])
                    .parseClaimsJws(token)  // 0.9.1 版本直接调用 parseClaimsJws()
                    .getBody();  // 0.9.1 版本使用 getBody()

            // 检查是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret().getBytes())  // 0.9.1 版本使用 setSigningKey(byte[])
                    .parseClaimsJws(token)  // 0.9.1 版本直接调用 parseClaimsJws()
                    .getBody();  // 0.9.1 版本使用 getBody()

            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("从Token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    // 添加黑名单方法
    public void addToBlacklist(String token) {
        synchronized (lock) {
            tokenBlacklist.add(token);
            log.info("Token已添加到黑名单");
        }
    }

    // 检查 token 是否在黑名单中
    public boolean isTokenBlacklisted(String token) {
        synchronized (lock) {
            return tokenBlacklist.contains(token);
        }
    }

    // 可选：从黑名单中移除 token（如果需要）
    public void removeFromBlacklist(String token) {
        synchronized (lock) {
            tokenBlacklist.remove(token);
            log.info("Token已从黑名单移除");
        }
    }
}
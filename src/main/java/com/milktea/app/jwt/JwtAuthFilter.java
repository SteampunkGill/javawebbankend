package com.milktea.app.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        Long userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.debug("JWT Token received: {}", token);

            try {
                if (jwtService.validateToken(token)) {
                    userId = jwtService.getUserIdFromToken(token);
                    log.debug("Valid JWT for user ID: {}", userId);
                } else {
                    log.warn("Invalid JWT token");
                }
            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
            }
        }

        // 关键修改：即使已经有Authentication，也要用JWT的认证覆盖它
        if (userId != null) {
            UserDetails userDetails = createUserDetails(userId);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authentication set for user ID: {}", userId);
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails createUserDetails(Long userId) {
        List<SimpleGrantedAuthority> authorities = Collections.emptyList();

        return new User(
                userId.toString(),
                "",
                true,
                true,
                true,
                true,
                authorities
        );
    }
}
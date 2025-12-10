// File: milktea-backend/src/main/java/com.milktea.app/config/WebSocketConfig.java
package com.milktea.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use /topic for broadcasting messages to many users (e.g., order status updates)
        // Use /queue for sending messages to a specific user (e.g., direct notifications)
        config.enableSimpleBroker("/topic", "/queue");
        // /app is the prefix for client-to-server messages
        config.setApplicationDestinationPrefixes("/app");
        // /user prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the /ws/order endpoint for WebSocket connections as per frontend doc
        registry.addEndpoint("/ws/order") // Changed endpoint to match /v1/ws/order from doc
                .setAllowedOriginPatterns("*") // Allow all origins, adjust for production
                .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSockets
    }
}
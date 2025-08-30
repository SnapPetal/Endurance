package com.thonbecker.endurance.config;

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
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Keep the existing SockJS endpoint
        registry.addEndpoint("/quiz-websocket").setAllowedOriginPatterns("*").withSockJS();

        // Add a raw WebSocket endpoint (same path but without SockJS wrapping)
        registry.addEndpoint("/quiz-websocket").setAllowedOriginPatterns("*");
    }
}

package com.putoet.mybooks.books.application.security.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import java.time.Instant;

@Configuration
public class SecurityEventConfiguration {
    @Bean
    public ApplicationListener<SecurityEvent> securityEventListener() {
        return event -> System.err.println(event.auditMessage());
    }

    @Bean
    public ApplicationListener<AuthenticationSuccessEvent> authenticationSuccessEventListener() {
        final var name = "SUCCESSFUL_USER_LOGIN";
        return event -> {
            Authentication authentication = event.getAuthentication();
            final var user = authentication != null ? authentication.getName() : "unknown";
            final var timestamp = Instant.ofEpochMilli(event.getTimestamp());
            System.err.printf("%s: event %s, details %s%n", timestamp, name, user);
        };
    }

    @Bean
    public ApplicationListener<AbstractAuthenticationFailureEvent> authenticationFailureEventListener() {
        return event -> {
            final var name = "FAILED_USER_LOGIN (" + event.getClass().getSimpleName() + ")";
            Authentication authentication = event.getAuthentication();
            final var user = authentication != null ? authentication.getName() : "unknown";
            final var timestamp = Instant.ofEpochMilli(event.getTimestamp());
            System.err.printf("%s: event %s, details %s%n", timestamp, name, user);
        };
    }
}

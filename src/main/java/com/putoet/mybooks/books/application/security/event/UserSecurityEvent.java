package com.putoet.mybooks.books.application.security.event;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

public class UserSecurityEvent extends SecurityEvent {
    private final String username;

    protected UserSecurityEvent(@NonNull Object source, @NonNull String name) {
        super(source, name);

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        this.username = authentication == null ? "anonymous" : authentication.getName();
    }

    protected UserSecurityEvent(@NonNull Object source, @NonNull String name, @NonNull String details) {
        super(source, name, details);

        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        this.username = authentication == null ? "anonymous" : authentication.getName();
    }

    @Override
    public String auditMessage() {
        final var timestamp = Instant.ofEpochMilli(getTimestamp());
        return details.isEmpty() ? String.format("%s: event %s by user %s", timestamp, name, username)
                : String.format("%s: event %s by user %s, details %s", timestamp, name, username, details);
    }
}

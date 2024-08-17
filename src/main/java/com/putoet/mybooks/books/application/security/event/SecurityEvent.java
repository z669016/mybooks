package com.putoet.mybooks.books.application.security.event;


import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;

import java.time.Instant;

public class SecurityEvent extends ApplicationEvent {
    protected final String name;
    protected final String details;

    protected SecurityEvent(@NonNull Object source, @NonNull String name) {
        super(source);
        this.name = name;
        this.details = "";
    }

    protected SecurityEvent(@NonNull Object source, @NonNull String name, @NonNull String details) {
        super(source);
        this.name = name;
        this.details = details;
    }

    public String auditMessage() {
        final var timestamp = Instant.ofEpochMilli(getTimestamp());
        return details.isEmpty() ? String.format("%s: event %s", timestamp, name)
                : String.format("%s: event %s, details %s", timestamp, name, details);
    }

    @Override
    public String toString() {
        return auditMessage();
    }
}

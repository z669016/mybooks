package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.validation.VersionConstraint;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpdateAuthorRequest(@VersionConstraint String version, @NotBlank String name) {
    public Instant versionAsInstant() {
        return Instant.parse(version());
    }
}

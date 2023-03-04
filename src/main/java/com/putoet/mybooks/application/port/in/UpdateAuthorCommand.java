package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.AuthorId;

public record UpdateAuthorCommand(AuthorId id, String name) {
    public UpdateAuthorCommand {
        if (id == null)
            ServiceError.AUTHOR_ID_REQUIRED.raise();

        if (name == null || name.isBlank())
            ServiceError.AUTHOR_NAME_REQUIRED.raise();
    }
}

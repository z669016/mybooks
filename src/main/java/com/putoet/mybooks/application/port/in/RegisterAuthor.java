package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;

public interface RegisterAuthor {
    Author registerAuthor(RegisterAuthorCommand command);
}

package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;

public interface UpdateAuthor {
    Author updateAuthor(AuthorId authorId, String name);
}

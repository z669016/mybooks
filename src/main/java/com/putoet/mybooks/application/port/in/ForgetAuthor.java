package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.AuthorId;

public interface ForgetAuthor {
    void forgetAuthor(AuthorId authorId);
}

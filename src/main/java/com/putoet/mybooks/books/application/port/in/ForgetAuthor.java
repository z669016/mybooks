package com.putoet.mybooks.books.application.port.in;

import com.putoet.mybooks.books.domain.AuthorId;

public interface ForgetAuthor {
    void forgetAuthor(AuthorId authorId);
}

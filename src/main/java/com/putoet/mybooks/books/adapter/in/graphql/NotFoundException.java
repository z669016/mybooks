package com.putoet.mybooks.books.adapter.in.graphql;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String id) {
        super(id);
    }
}

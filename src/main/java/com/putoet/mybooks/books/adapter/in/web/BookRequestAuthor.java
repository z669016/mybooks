package com.putoet.mybooks.books.adapter.in.web;

import java.util.Map;

public record BookRequestAuthor(String id, String name, Map<String, String> sites) {
    public boolean isNewRequest() {
        return id == null;
    }

    public boolean isExistingRequest() {
        return id != null;
    }

    public NewAuthorRequest newAuthorRequest() {
        if (!isNewRequest())
            throw new IllegalStateException("Cannot handle author request as new");

        return new NewAuthorRequest(name, sites);
    }

    public ExistingAuthorRequest existingAuthorRequest() {
        if (!isExistingRequest())
            throw new IllegalStateException("Cannot handle author request as existing");

        return new ExistingAuthorRequest(id);
    }
}

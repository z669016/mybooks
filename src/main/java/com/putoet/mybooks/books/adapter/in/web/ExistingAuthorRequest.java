package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.validation.ObjectIDConstraint;

public record ExistingAuthorRequest(@ObjectIDConstraint String id) {
}

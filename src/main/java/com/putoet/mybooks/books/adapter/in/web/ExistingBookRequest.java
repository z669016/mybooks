package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.adapter.in.web.validation.ExistingBookRequestConstraint;

@ExistingBookRequestConstraint
public record ExistingBookRequest(String schema, String id) {
}

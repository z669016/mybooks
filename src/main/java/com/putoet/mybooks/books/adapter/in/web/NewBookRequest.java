package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.MimeTypes;
import jakarta.activation.MimeType;

import java.util.Set;
import java.util.stream.Collectors;

public record NewBookRequest(String schema, String id, String title, Set<BookRequestAuthor> authors, Set<String> keywords, Set<String> formats) {

    public Set<MimeType> formatsAsMimeTypeList() {
        return formatsAsMimeTypeList(formats);
    }

    public static Set<MimeType> formatsAsMimeTypeList(Set<String> formats) {
        return formats.stream()
                .map(MimeTypes::toMimeType)
                .collect(Collectors.toSet());
    }
}

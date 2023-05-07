package com.putoet.mybooks.books.adapter.in.web;

import com.putoet.mybooks.books.domain.MimeTypes;
import jakarta.activation.MimeType;

import java.util.List;
import java.util.Set;

public record NewBookRequest(String schema, String id, String title, List<BookRequestAuthor> authors, Set<String> keywords, List<String> formats) {

    public List<MimeType> formatsAsMimeTypeList() {
        return formatsAsMimeTypeList(formats);
    }

    public static List<MimeType> formatsAsMimeTypeList(List<String> formats) {
        return formats.stream()
                .map(MimeTypes::toMimeType)
                .toList();
    }
}

package com.putoet.mybooks.books.domain;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.util.*;

/**
 * Class MimeTypes
 * Container class for available book formats, where book format is represented by a MimeType
 */
public class MimeTypes {
    public static final MimeType EPUB;
    public static final MimeType PDF;
    public static final MimeType MOBI;

    private static final Map<String,MimeType> cache = new HashMap<>();

    static {
        EPUB = toMimeType("application/epub+zip");
        PDF = toMimeType("application/pdf");
        MOBI = toMimeType("application/x-mobipocket-ebook");
    }

    public static MimeType toMimeType(String mimeType) {
        Objects.requireNonNull(mimeType);

        return cache.computeIfAbsent(mimeType, MimeTypes::newMimeType);
    }

    private static MimeType newMimeType(String mimeType) {
        try {
            return new MimeType(mimeType);
        } catch (MimeTypeParseException exc) {
            throw new IllegalArgumentException(exc);
        }
    }
}

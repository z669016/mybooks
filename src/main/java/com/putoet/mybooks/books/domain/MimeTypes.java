package com.putoet.mybooks.books.domain;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class MimeTypes
 * Container class for available book formats, where book format is represented by a MimeType
 */
public class MimeTypes {
    private static final Logger logger = LoggerFactory.getLogger(MimeTypes.class);

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
            logger.error("Invalid mime type '{}'", mimeType);
            throw new IllegalArgumentException(exc);
        }
    }

    private final Set<MimeType> mimeTypes;

    public MimeTypes() {
        this.mimeTypes = Set.of();
    }

    public MimeTypes(Set<MimeType> mimeTypes) {
        Objects.requireNonNull(mimeTypes);

        this.mimeTypes = new HashSet<>(mimeTypes);
    }

    public boolean contains(MimeType mimeType) {
        Objects.requireNonNull(mimeType);

        final String name = mimeType.toString();
        return mimeTypes.stream().map(MimeType::toString).filter(t -> t.equals(name)).count() == 1;
    }

    public MimeTypes add(MimeType mimeType) {
        Objects.requireNonNull(mimeType);

        if (mimeTypes.contains(mimeType))
            throw new IllegalArgumentException("Book already contains format " + mimeType);

        var updated = new HashSet<>(mimeTypes);
        updated.add(mimeType);

        return new MimeTypes(updated);
    }

    public Set<MimeType> mimeTypes() {
        return Collections.unmodifiableSet(mimeTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MimeTypes other)) return false;

        if (this.mimeTypes.size() != other.mimeTypes.size()) return false;

        final List<String> thisTypes = this.mimeTypes.stream().map(MimeType::toString).sorted().toList();
        final List<String> otherTypes = other.mimeTypes.stream().map(MimeType::toString).sorted().toList();

        return thisTypes.equals(otherTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mimeTypes);
    }
}

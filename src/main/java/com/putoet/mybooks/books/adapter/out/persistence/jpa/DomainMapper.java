package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DomainMapper {
    Author toDomain(AuthorEntity entity) {
        final var sites = new HashMap<SiteType, URL>();
        for (var entry : entity.getSites().entrySet()) {
            try {
                final var siteType = SiteType.of(entry.getKey());
                final var url = new URL(entry.getValue());
                sites.put(siteType, url);
            } catch (MalformedURLException exc) {
                log.error("Author entity with key {} has an invalid URL {} for site type {} which will be ignored", entity.getAuthorId(), entry.getValue(), entry.getKey());
            }
        }

        return new Author(new AuthorId(entity.getAuthorId()), entity.getVersion(), entity.getName(), Collections.unmodifiableMap(sites));
    }

    Book toDomain(BookEntity entity) {
        final var bookId = new BookId(entity.getBookId().getIdType(), entity.getBookId().getId());
        return new Book(
                bookId,
                entity.getTitle(),
                entity.getAuthors().stream().map(this::toDomain).collect(Collectors.toSet()),
                entity.getKeywords(),
                entity.getFormats().stream().map(MimeTypes::toMimeType).collect(Collectors.toSet())
        );
    }

    AuthorEntity fromDomain(Author author) {
        final var entity = new AuthorEntity();
        if (Objects.nonNull(author.id())) entity.setAuthorId(author.id().uuid());
        if (Objects.nonNull(author.version())) entity.setVersion(author.version());
        if (Objects.nonNull(author.name())) entity.setName(author.name());
        if (Objects.nonNull(author.sites())) {
            entity.setSites(author.sites().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
        }

        return entity;
    }

    BookEntity fromDomain(Book book) {
        final var entity = new BookEntity();
        entity.setBookId(new BookIdEntity(book.id().schema().name(), book.id().id()));
        entity.setTitle(book.title());
        entity.setAuthors(book.authors().stream().map(this::fromDomain).collect(Collectors.toSet()));
        entity.setKeywords(book.keywords());
        entity.setFormats(book.formats().stream().map(MimeType::toString).collect(Collectors.toSet()));

        return entity;
    }
}

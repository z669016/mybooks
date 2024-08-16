package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Profile("jpa")
public class JpaBookRepository implements BookPersistenceUpdatePort {
    private final DomainMapper mapper;
    private final AuthorJpaRepository authorRepository;
    private final BookJpaRepository bookRepository;

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        final var author = new AuthorEntity();
        author.setAuthorId(UUID.randomUUID());
        author.setName(name);
        author.setVersion(Instant.now());
        author.setSites(sites.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().toString())));
        return mapper.toDomain(authorRepository.save(author));
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        final var author = authorRepository.findById(authorId.uuid());

        if (author.isEmpty() || author.get().getVersion().compareTo(version) != 0)
            throw ServiceError.AUTHOR_NOT_UPDATED.exception(authorId + ", " + version + ", '" + name + "'");

        author.get().setName(name);
        author.get().setVersion(Instant.now());
        return mapper.toDomain(authorRepository.save(author.get()));
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        authorRepository.deleteById(authorId.uuid());
    }

    @Override
    public Author setAuthorSite(AuthorId id, SiteType type, URL url) {
        final var author = authorRepository.findById(id.uuid());
        if (author.isEmpty())
            return null;

        final var authorEntity = author.get();
        authorEntity.getSites().put(type.name(), url.toString());
        return mapper.toDomain(authorRepository.save(authorEntity));
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        final BookEntity book = new BookEntity();
        book.setBookId(new BookIdEntity(bookId.schema().name(), bookId.id()));
        book.setTitle(title);
        book.setAuthors(authors.stream().map(mapper::fromDomain).collect(Collectors.toSet()));
        book.setFormats(formats.stream().map(MimeType::toString).collect(Collectors.toSet()));
        book.setKeywords(keywords);

        final var newBook = bookRepository.save(book);
        return mapper.toDomain(newBook);
    }

    @Override
    public Set<Author> findAuthors() {
        final var authors = authorRepository.findAll();
        return Authors.ordered(authors.stream().map(mapper::toDomain).collect(Collectors.toSet()));
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        final var authors = authorRepository.findAuthorEntityByNameContainsIgnoreCase(name);
        return Authors.ordered(authors.stream().map(mapper::toDomain).collect(Collectors.toSet()));
    }

    @Override
    public Author findAuthorById(AuthorId authorId) {
        final var author = authorRepository.findById(authorId.uuid());
        return author.map(mapper::toDomain).orElse(null);
    }

    @Override
    public Set<Book> findBooks() {
        return bookRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        return bookRepository.findBookEntityByTitleContainsIgnoreCase(title).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public Book findBookById(BookId bookId) {
        return bookRepository.findById(new BookIdEntity(bookId.schema().name(), bookId.id()))
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        final var author = authorRepository.findById(authorId.uuid());
        return author.map(authorEntity -> bookRepository.findBookEntityByAuthorsContains(authorEntity).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }
}

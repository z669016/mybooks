package com.putoet.mybooks.books.adapter.out.persistence.jpa;

import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("jpa")
public class JpaBookRepository implements BookPersistenceUpdatePort {
    public static final Logger log = LoggerFactory.getLogger(JpaBookRepository.class);

    private final DomainMapper mapper;
    private final AuthorJpaRepository authorRepository;
    private final BookJpaRepository bookRepository;

    public JpaBookRepository(DomainMapper mapper, AuthorJpaRepository authorRepository, BookJpaRepository bookRepository) {
        this.mapper = mapper;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        log.debug("JpaBookRepository('{}','{}','{}')", mapper, authorRepository, bookRepository);
    }

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        log.debug("registerAuthor('{}','{}')", name, sites);
        final var author = new AuthorEntity();
        author.setAuthorId(UUID.randomUUID());
        author.setName(name);
        author.setVersion(Instant.now());
        author.setSites(sites.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().toString())));

        final var result = mapper.toDomain(authorRepository.save(author));
        log.debug("register author returns: {}", result);
        return result;
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        log.debug("updateAuthor('{}','{}','{}')", authorId, version, name);
        final var author = authorRepository.findById(authorId.uuid());

        if (author.isEmpty() || author.get().getVersion().compareTo(version) != 0)
            throw ServiceError.AUTHOR_NOT_UPDATED.exception(authorId + ", " + version + ", '" + name + "'");

        author.get().setName(name);
        author.get().setVersion(Instant.now());

        final var result = mapper.toDomain(authorRepository.save(author.get()));
        log.debug("update author returns: {}", result);
        return result;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        log.debug("forgetAuthor('{}')", authorId);

        authorRepository.deleteById(authorId.uuid());
    }

    @Override
    public Author setAuthorSite(AuthorId id, SiteType type, URL url) {
        log.debug("setAuthorSite('{}','{}','{}')", id, type, url);

        final var author = authorRepository.findById(id.uuid());
        if (author.isEmpty()) {
            log.debug("setAuthorSite returns: null");
            return null;
        }

        final var authorEntity = author.get();
        authorEntity.getSites().put(type.name(), url.toString());
        final var result = mapper.toDomain(authorRepository.save(authorEntity));
        log.debug("setAuthorSite returns: {}", result);

        return result;
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        log.debug("registerBook('{}','{}','{}','{}','{}')", bookId, title, authors, formats, keywords);

        final BookEntity book = new BookEntity();
        book.setBookId(new BookIdEntity(bookId.schema().name(), bookId.id()));
        book.setTitle(title);
        book.setAuthors(authors.stream().map(mapper::fromDomain).collect(Collectors.toSet()));
        book.setFormats(formats.stream().map(MimeType::toString).collect(Collectors.toSet()));
        book.setKeywords(keywords);

        final var newBook = bookRepository.save(book);
        final var result = mapper.toDomain(newBook);
        log.debug("registerBook returns: {}", result);

        return result;
    }

    @Override
    public Set<Author> findAuthors() {
        log.debug("findAuthors()");

        final var authors = authorRepository.findAll();
        final var result = Authors.ordered(authors.stream().map(mapper::toDomain).collect(Collectors.toSet()));
        log.debug("findAuthors returns: {}", result);

        return result;
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        log.debug("findAuthorsByName('{}')", name);

        final var authors = authorRepository.findAuthorEntityByNameContainsIgnoreCase(name);
        final var result = Authors.ordered(authors.stream().map(mapper::toDomain).collect(Collectors.toSet()));
        log.debug("find authors by name returns: {}", result);

        return result;
    }

    @Override
    public Author findAuthorById(AuthorId authorId) {
        final var author = authorRepository.findById(authorId.uuid());
        return author.map(mapper::toDomain).orElse(null);
    }

    @Override
    public Set<Book> findBooks() {
        log.debug("findBooks()");

        final var books = bookRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
        log.debug("find books returns: {}", books);

        return books;
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        log.debug("findBooksByTitle('{}')", title);

        final var books = bookRepository.findBookEntityByTitleContainsIgnoreCase(title).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
        log.debug("find books by title returns: {}", books);

        return books;
    }

    @Override
    public Book findBookById(BookId bookId) {
        log.debug("findBookById('{}')", bookId);

        final var book = bookRepository.findById(new BookIdEntity(bookId.schema().name(), bookId.id()))
                .map(mapper::toDomain)
                .orElse(null);
        log.debug("find book by id returns: {}", book);

        return book;
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        log.debug("findBooksByAuthorId('{}')", authorId);

        final var author = authorRepository.findById(authorId.uuid());
        final var result = author.map(authorEntity -> bookRepository.findBookEntityByAuthorsContains(authorEntity).stream()
                        .map(mapper::toDomain)
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);

        log.debug("find books by author id returns: {}", result);
        return result;
    }
}

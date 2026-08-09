package com.putoet.mybooks.books.adapter.out.persistence.jdbc;

import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.putoet.mybooks.books.adapter.out.persistence.jdbc.SqlUtil.sqlInfo;

/**
 * Class H2BookRepository
 * A read/write repository for book and author data, connected to an H4 database using a Spring JdbcTemplate
 */
@Repository
@Profile("jdbc")
public class H2BookRepository implements BookPersistenceUpdatePort {
    public static final Logger log = LoggerFactory.getLogger(H2BookRepository.class);

    private final JdbcTemplate template;

    public H2BookRepository(JdbcTemplate template) {
        log.debug("H2BookRepository('{}')", template);
        this.template = template;
    }

    @Override
    public String toString() {
        try (var connection = Objects.requireNonNull(template.getDataSource()).getConnection()) {
            return String.format("%s(%s)", this.getClass().getName(), connection.getMetaData().getURL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Author> findAuthors() {
        log.debug("findAuthors()");

        final String sql = "select author_id, version, name from author";
        sqlInfo(log, sql);

        final var authors = Authors.ordered(template.query(sql, this::authorMapper));
        log.debug("find authors returns: {}", authors);
        return authors;
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        log.debug("findAuthorsByName('{}')", name);
        name = "%" + name.toLowerCase() + "%";

        final String sql = "select author_id, version, name from author where lower(name) like ?";
        sqlInfo(log, sql, name);

        final var authors = template.query(sql, this::authorMapper, name);
        log.debug("find authors by name returns: {}", authors);
        return Authors.ordered(authors);
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        log.debug("findAuthorById('{}')", id);

        try {
            final String sql = "select author_id, version, name from author where author_id = ?";
            sqlInfo(log, sql, id.uuid());

            final var author = template.queryForObject(sql, this::authorMapper, id.uuid());
            log.debug("find author by id returns: {}", author);
            return author;

        } catch (EmptyResultDataAccessException exc) {
            log.warn(exc.getMessage());
        }
        return null;
    }

    @Override
    public Set<Book> findBooks() {
        log.debug("findBooks()");

        final String sql = "select book_id_type, book_id, title from book";
        sqlInfo(log, sql);

        final var books = Books.ordered(template.query(sql, this::bookMapper));
        log.debug("find books returns: {}", books);
        return books;
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        log.debug("findBooksByTitle('{}')", title);

        if (title == null || title.isBlank()) {
            log.error(ServiceError.BOOK_TITLE_REQUIRED.name());
            throw ServiceError.BOOK_TITLE_REQUIRED.exception();
        }

        title = "%" + title.toLowerCase() + "%";
        final String sql = "select book_id_type, book_id, title from book where lower(title) like ?";
        sqlInfo(log, sql, title);

        final var books = Books.ordered(template.query(sql, this::bookMapper, title));
        log.debug("find books by title returns: {}", books);
        return books;
    }

    @Override
    public Book findBookById(BookId bookId) {
        log.debug("findBookById('{}')", bookId);

        if (bookId == null) {
            log.error(ServiceError.BOOK_ID_REQUIRED.name());
            throw ServiceError.BOOK_ID_REQUIRED.exception();
        }

        final String sql = "select book_id_type, book_id, title from book where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookId.schema().name(), bookId.id());
        final var book = template.queryForObject(sql, this::bookMapper, bookId.schema().name(), bookId.id());
        log.debug("find book by id returns: {}", book);
        return book;
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        log.debug("findBooksByAuthorId('{}')", authorId);

        final String sql = "select book_id_type, book_id, title from book where (book_id_type, book_id) in (select book_id_type, book_id from book_author where author_id = ?)";
        sqlInfo(log, sql, authorId.uuid());

        final var books = Books.ordered(template.query(sql, this::bookMapper, authorId.uuid()));
        log.debug("find books by author id returns: {}", books);
        return books;
    }

    private Book bookMapper(ResultSet row, int rowNum) throws SQLException {
        final var book_id_type = row.getString("book_id_type");
        final var book_id = row.getString("book_id");
        final var authors = findAuthorsForBook(book_id_type, book_id);
        final var formats = findFormatsForBook(book_id_type, book_id);
        final var keywords = findKeywordsForBook(book_id_type, book_id);

        return new Book(new BookId(BookId.BookIdSchema.valueOf(book_id_type), book_id)
                , row.getString("title")
                , authors
                , keywords
                , formats
        );
    }

    private Set<String> findKeywordsForBook(String bookIdType, String bookId) {
        log.debug("findKeywordsForBook('{}', '{}')", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, keyword from book_key_word where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookIdType, bookId);

        final var keywords = template.query(sql, this::keywordMapper, bookIdType, bookId);
        log.debug("find keywords for book returns: {}", keywords);
        return Set.copyOf(keywords);
    }

    private String keywordMapper(ResultSet row, int rowNum) throws SQLException {
        return row.getString("keyword");
    }

    private Set<MimeType> findFormatsForBook(String bookIdType, String bookId) {
        log.debug("findFormatsForBook('{}', '{}')", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, format from book_format where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookIdType, bookId);

        final var formats = template.query(sql, this::formatTypeMapper, bookIdType, bookId);
        log.debug("find formats for book returns: {}", formats);
        return Set.copyOf(formats);
    }

    private MimeType formatTypeMapper(ResultSet row, int rowNum) throws SQLException {
        final String format = row.getString("format");
        return MimeTypes.toMimeType(format);
    }

    private Set<Author> findAuthorsForBook(String bookIdType, String bookId) {
        log.debug("findAuthorsForBook('{}', '{}')", bookIdType, bookId);

        final String sql = "select author_id, version, name from author where author_id in (select author_id from book_author where book_id_type = ? and book_id = ?)";
        sqlInfo(log, sql, bookIdType, bookId);

        final var authors = template.query(sql, this::authorMapper, bookIdType, bookId);
        log.debug("find authors for book returns: {}", authors);
        return Set.copyOf(authors);
    }

    private Author authorMapper(ResultSet row, int rowNum) throws SQLException {
        final String authorId = row.getString("author_id");
        final String sql = "select name, url from site where author_id = ?";
        sqlInfo(log, sql, authorId);

        final var sites = template.query(sql, this::siteMapper, authorId);
        return new Author(AuthorId.withId(authorId),
                row.getTimestamp("version").toInstant(),
                row.getString("name"),
                sites.stream().collect(Collectors.toMap(Site::type, Site::url))
        );
    }

    private Site siteMapper(ResultSet row, int rowNum) throws SQLException {
        try {
            final var type = SiteType.of(row.getString("name"));
            final var url = new URL(row.getString("url"));
            return new Site(type, url);
        } catch (MalformedURLException exc) {
            throw new SQLException("Invalid URL for site " + row, exc);
        }
    }

    @Override
    public Author registerAuthor(String name, Map<SiteType, URL> sites) {
        log.debug("registerAuthor('{}', '{}')", name, sites);

        final var author = registerAuthor(AuthorId.withoutId(), Instant.now(), name, sites);
        log.debug("registerAuthor returns: {}", author);
        return author;
    }

    public Author registerAuthor(AuthorId authorId, Instant version, String name, Map<SiteType, URL> sites) {
        final String sql = "insert into author (author_id, version, name) values (?, ?, ?)";
        sqlInfo(log, sql, authorId.uuid(), version, name);

        int count = template.update(sql, authorId.uuid(), version, name);
        if (count != 1) {
            final var details = "author id '" + authorId + "', name '" + name + "', and version '" + version + "'";
            log.error("Could not insert author (count is '{}'): {}", count, details);
            throw ServiceError.AUTHOR_NOT_REGISTERED.exception(details);
        }

        for (var site : sites.entrySet()) {
            setAuthorSite(authorId, site.getKey(), site.getValue());
        }

        return findAuthorById(authorId);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        log.debug("updateAuthor('{}', '{}')", authorId, name);

        final var newVersion = Timestamp.from(Instant.now());
        final String sql = "update author set version = ?, name = ? where author_id = ? and version = ?";
        sqlInfo(log, sql, newVersion, name, authorId.uuid(), version);

        int count = template.update(sql, newVersion, name, authorId.uuid(), version);
        if (count != 1) {
            final var details = "author id '" + authorId + "', name '" + name + "', and version '" + version + "'";
            log.error("Could not update author (count is '{}'): {}", count, details);
            throw ServiceError.AUTHOR_NOT_UPDATED.exception(details);
        }

        final var author = findAuthorById(authorId);
        log.debug("updateAuthor returns: {}", author);
        return author;
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        log.debug("forgetAuthor('{}')", authorId);

        final String sql = "delete from author where author_id = ?";
        sqlInfo(log, sql, authorId);

        int count = template.update(sql, authorId.uuid());
        if (count != 1) {
            final var details = "author id '" + authorId + "'";
            log.error("Could not delete author (count is '{}'): {}", count, details);
            throw ServiceError.AUTHOR_FOR_ID_NOT_FOUND.exception(details);
        }
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        log.debug("setAuthorSite('{}', '{}', '{}')", authorId, type, url);

        final String sql = "merge into site (author_id, name, url) values (?, ?, ?)";
        sqlInfo(log, sql, authorId.uuid(), type.name(), url.toString());

        int count = template.update(sql, authorId.uuid(), type.name(), url.toString());
        if (count != 1) {
            final var details = "authorId '" + authorId + "', site type '" + type + "' and site url is '" + url + "'";
            log.error("Could not merge site into author: {}", details);
            throw ServiceError.AUTHOR_SITE_NOT_SET.exception(details);
        }

        final var author = findAuthorById(authorId);
        log.debug("setAuthorSite returns: {}", author);
        return author;
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        log.debug("registerBook('{}', '{}', '{}', '{}')", bookId, title, authors, formats);

        final String sql = "insert into book (book_id_type, book_id, title) values (?, ?, ?)";
        sqlInfo(log, sql, bookId.schema().name(), bookId.id(), title);

        int count = template.update(sql, bookId.schema().name(), bookId.id(), title);
        if (count != 1) {
            final var details = "book id '" + bookId + "', title '" + title + "'";
            log.error("Could not insert book (count is {}): {}", count, details);
            throw ServiceError.BOOK_NOT_REGISTERED.exception(details);
        }

        for (var author : authors) {
            final String sql2 = "insert into book_author (book_id_type, book_id, author_id) values (?, ?, ?)";
            sqlInfo(log, sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());
            if (count != 1) {
                final var details = "book id '" + bookId + "', author id '" + author.id() + "'";
                log.error("Could not insert book author (count is {}): {}", count, details);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(details);
            }
        }

        for (MimeType format : formats) {
            final String sql2 = "insert into book_format (book_id_type, book_id, format) values (?, ?, ?)";
            sqlInfo(log, sql2, bookId.schema().name(), bookId.id(), format.toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), format.toString());
            if (count != 1) {
                final var details = "book id '" + bookId + "', format id '" + format + "'";
                log.error("Could not insert book format (count is {}): {}", count, details);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(details);
            }
        }

        for (String keyword : keywords) {
            final String sql3 = "insert into book_key_word (book_id_type, book_id, keyword) values (?, ?, ?)";
            sqlInfo(log, sql3, bookId.schema().name(), bookId.id(), keyword);

            count = template.update(sql3, bookId.schema().name(), bookId.id(), keyword);
            if (count != 1) {
                final var details = "book id '" + bookId + "', keyword '" + keyword + "'";
                log.error("Could not insert book keywords (count is {}): {}", count, details);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(details);
            }
        }

        final var book = findBookById(bookId);
        log.debug("registerBook returns: {}", book);
        return book;
    }
}

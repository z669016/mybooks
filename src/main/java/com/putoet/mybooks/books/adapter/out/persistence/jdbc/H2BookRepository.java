package com.putoet.mybooks.books.adapter.out.persistence.jdbc;

import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
@Profile("jdbc")
public class H2BookRepository implements BookPersistenceUpdatePort {

    private final JdbcTemplate template;

    @SneakyThrows
    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getName(),
                Objects.requireNonNull(template.getDataSource()).getConnection().getMetaData().getURL());
    }

    @Override
    public Set<Author> findAuthors() {
        log.info("findAuthors()");

        final String sql = "select author_id, version, name from author";
        sqlInfo(log, sql);

        return Authors.ordered(template.query(sql, this::authorMapper));
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        log.info("findAuthorsByName({})", name);
        name = "%" + name.toLowerCase() + "%";

        final String sql = "select author_id, version, name from author where lower(name) like ?";
        sqlInfo(log, sql, name);

        return Authors.ordered(template.query(sql, this::authorMapper, name));
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        log.info("findAuthorById({})", id);

        try {
            final String sql = "select author_id, version, name from author where author_id = ?";
            sqlInfo(log, sql, id.uuid());

            return template.queryForObject(sql, this::authorMapper, id.uuid());
        } catch (EmptyResultDataAccessException exc) {
            log.warn(exc.getMessage());
        }
        return null;
    }

    @Override
    public Set<Book> findBooks() {
        log.info("findBooks()");

        final String sql = "select book_id_type, book_id, title from book";
        sqlInfo(log, sql);

        return Books.ordered(template.query(sql, this::bookMapper));
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        log.info("findBooksByTitle({})", title);

        if (title == null || title.isBlank()) {
            log.error(ServiceError.BOOK_TITLE_REQUIRED.name());
            throw ServiceError.BOOK_TITLE_REQUIRED.exception();
        }

        title = "%" + title.toLowerCase() + "%";
        final String sql = "select book_id_type, book_id, title from book where lower(title) like ?";
        sqlInfo(log, sql, title);

        return Books.ordered(template.query(sql, this::bookMapper, title));
    }

    @Override
    public Book findBookById(BookId bookId) {
        log.info("findBookById({})", bookId);

        if (bookId == null) {
            log.error(ServiceError.BOOK_ID_REQUIRED.name());
            throw ServiceError.BOOK_ID_REQUIRED.exception();
        }

        final String sql = "select book_id_type, book_id, title from book where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookId.schema().name(), bookId.id());
        return template.queryForObject(sql, this::bookMapper, bookId.schema().name(), bookId.id());
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        log.info("findBooksByAuthorId({})", authorId);

        final String sql = "select book_id_type, book_id, title from book where (book_id_type, book_id) in (select book_id_type, book_id from book_author where author_id = ?)";
        sqlInfo(log, sql, authorId.uuid());

        return Books.ordered(template.query(sql, this::bookMapper, authorId.uuid()));
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
        log.info("findKeywordsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, keyword from book_key_word where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::keywordMapper, bookIdType, bookId));
    }

    private String keywordMapper(ResultSet row, int rowNum) throws SQLException {
        return row.getString("keyword");
    }

    private Set<MimeType> findFormatsForBook(String bookIdType, String bookId) {
        log.info("findFormatsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, format from book_format where book_id_type = ? and book_id = ?";
        sqlInfo(log, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::formatTypeMapper, bookIdType, bookId));
    }

    private MimeType formatTypeMapper(ResultSet row, int rowNum) throws SQLException {
        final String format = row.getString("format");
        return MimeTypes.toMimeType(format);
    }

    private Set<Author> findAuthorsForBook(String bookIdType, String bookId) {
        log.info("findAuthorsForBook({}, {})", bookIdType, bookId);

        final String sql = "select author_id, version, name from author where author_id in (select author_id from book_author where book_id_type = ? and book_id = ?)";
        sqlInfo(log, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::authorMapper, bookIdType, bookId));
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
        log.info("registerAuthor({}, {})", name, sites);
        return registerAuthor(AuthorId.withoutId(), Instant.now(), name, sites);
    }

    public Author registerAuthor(AuthorId id, Instant version, String name, Map<SiteType, URL> sites) {
        log.info("registerAuthor({}, {}, {}, {})", id, version, name, sites);

        final String sql = "insert into author (author_id, version, name) values (?, ?, ?)";
        sqlInfo(log, sql, id.uuid(), version, name);

        int count = template.update(sql, id.uuid(), version, name);
        if (count != 1) {
            log.error("{}: {} {} {}", ServiceError.AUTHOR_NOT_REGISTERED, id.uuid(), version, name);
            throw ServiceError.AUTHOR_NOT_REGISTERED.exception("Author with new id " + id + " and name " + name);
        }

        for (var type : sites.keySet()) {
            setAuthorSite(id, type, sites.get(type));
        }

        return findAuthorById(id);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        log.info("updateAuthor({}, {})", authorId, name);

        final var newVersion = Timestamp.from(Instant.now());
        final String sql = "update author set version = ?, name = ? where author_id = ? and version = ?";
        sqlInfo(log, sql, newVersion, name, authorId.uuid(), version);

        int count = template.update(sql, newVersion, name, authorId.uuid(), version);
        if (count != 1) {
            log.error("Could not update one author, count is {}, author id is {}, version is {}", count, authorId, version);
            throw ServiceError.AUTHOR_NOT_UPDATED.exception(authorId + ", " + version + ", '" + name + "'");
        }
        return findAuthorById(authorId);
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        log.info("forgetAuthor({})", authorId);

        final String sql = "delete from author where author_id = ?";
        sqlInfo(log, sql, authorId);

        int count = template.update(sql, authorId.uuid());
        if (count != 1) {
            log.error("{}: {}", ServiceError.AUTHOR_FOR_ID_NOT_FOUND.name(), authorId);
            throw ServiceError.AUTHOR_FOR_ID_NOT_FOUND.exception();
        }
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        log.info("setAuthorSite({}, {}, {})", authorId, type, url);

        final String sql = "merge into site (author_id, name, url) values (?, ?, ?)";
        sqlInfo(log, sql, authorId.uuid(), type.name(), url.toString());

        int count = template.update(sql, authorId.uuid(), type.name(), url.toString());
        if (count != 1) {
            log.error("{}: {} {}", ServiceError.AUTHOR_SITE_NOT_SET, authorId, type);
            throw ServiceError.AUTHOR_SITE_NOT_SET.exception(authorId + " " + type);
        }
        return findAuthorById(authorId);
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        log.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        final String sql = "insert into book (book_id_type, book_id, title) values (?, ?, ?)";
        sqlInfo(log, sql, bookId.schema().name(), bookId.id(), title);

        int count = template.update(sql, bookId.schema().name(), bookId.id(), title);
        if (count != 1) {
            log.error("{}: {}, {}, {}", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, title, authors);
            throw ServiceError.BOOK_NOT_REGISTERED.exception(bookId.toString());
        }

        for (var author : authors) {
            final String sql2 = "insert into book_author (book_id_type, book_id, author_id) values (?, ?, ?)";
            sqlInfo(log, sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());
            if (count != 1) {
                log.error("{}: {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, author);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(bookId + " " + author);
            }
        }

        for (MimeType format : formats) {
            final String sql2 = "insert into book_format (book_id_type, book_id, format) values (?, ?, ?)";
            sqlInfo(log, sql2, bookId.schema().name(), bookId.id(), format.toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), format.toString());
            if (count != 1) {
                log.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), format);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(bookId + " " + format);
            }
        }

        for (String keyword : keywords) {
            final String sql3 = "insert into book_key_word (book_id_type, book_id, keyword) values (?, ?, ?)";
            sqlInfo(log, sql3, bookId.schema().name(), bookId.id(), keyword);

            count = template.update(sql3, bookId.schema().name(), bookId.id(), keyword);
            if (count != 1) {
                log.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), keyword);
                throw ServiceError.BOOK_NOT_REGISTERED.exception(bookId + " " + keyword);
            }
        }

        return findBookById(bookId);
    }
}

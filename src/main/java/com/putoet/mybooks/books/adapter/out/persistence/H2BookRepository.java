package com.putoet.mybooks.books.adapter.out.persistence;

import com.putoet.mybooks.books.application.port.in.ServiceError;
import com.putoet.mybooks.books.application.port.out.persistence.BookPersistenceUpdatePort;
import com.putoet.mybooks.books.domain.*;
import jakarta.activation.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.putoet.mybooks.books.adapter.out.persistence.SqlUtil.sqlInfo;


/**
 * Class H2BookRepository
 * A read/write repository for book and author data, connected to an H4 database using a Spring JdbcTemplate
 */
@Repository
public class H2BookRepository implements BookPersistenceUpdatePort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate template;

    public H2BookRepository(JdbcTemplate template) {
        logger.info("AuthorRepository initialized with JDBC template {}", template.getDataSource());
        this.template = template;
    }


    @Override
    public Set<Author> findAuthors() {
        logger.info("findAuthors()");

        final String sql = "select author_id, version, name from author";
        sqlInfo(logger, sql);

        return Set.copyOf(template.query(sql, this::authorMapper));
    }

    @Override
    public Set<Author> findAuthorsByName(String name) {
        logger.info("findAuthorsByName({})", name);
        name = "%" + name.toLowerCase() + "%";

        final String sql = "select author_id, version, name from author where lower(name) like ?";
        sqlInfo(logger, sql, name);

        return Set.copyOf(template.query(sql, this::authorMapper, name));
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        logger.info("findAuthorById({})", id);

        try {
            final String sql = "select author_id, version, name from author where author_id = ?";
            sqlInfo(logger, sql, id.uuid());

            return template.queryForObject(sql, this::authorMapper, id.uuid());
        } catch (EmptyResultDataAccessException exc) {
            logger.warn(exc.getMessage());
        }
        return null;
    }

    @Override
    public Set<Book> findBooks() {
        logger.info("findBooks()");

        final String sql = "select book_id_type, book_id, title from book";
        sqlInfo(logger, sql);

        return Set.copyOf(template.query(sql, this::bookMapper));
    }

    @Override
    public Set<Book> findBooksByTitle(String title) {
        logger.info("findBooksByTitle({})", title);

        if (title == null || title.isBlank()) {
            logger.error(ServiceError.BOOK_TITLE_REQUIRED.name());
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        }

        title = "%" + title + "%";
        final String sql = "select book_id_type, book_id, title from book where title like ?";
        sqlInfo(logger, sql, title);

        return Set.copyOf(template.query(sql, this::bookMapper, title));
    }

    @Override
    public Book findBookById(BookId bookId) {
        logger.info("findBookById({})", bookId);

        if (bookId == null) {
            logger.error(ServiceError.BOOK_ID_REQUIRED.name());
            ServiceError.BOOK_ID_REQUIRED.raise();
        }

        final String sql = "select book_id_type, book_id, title from book where book_id_type = ? and book_id = ?";
        sqlInfo(logger, sql, bookId.schema().name(), bookId.id());
        return template.queryForObject(sql, this::bookMapper, bookId.schema().name(), bookId.id());
    }

    @Override
    public Set<Book> findBooksByAuthorId(AuthorId authorId) {
        logger.info("findBooksByAuthorId({})", authorId);

        final String sql = "select book_id_type, book_id, title from book where (book_id_type, book_id) in (select book_id_type, book_id from book_author where author_id = ?)";
        sqlInfo(logger, sql, authorId.uuid());

        return Set.copyOf(template.query(sql, this::bookMapper, authorId.uuid()));
    }

    private Book bookMapper(ResultSet row, int rowNum) throws SQLException {
        final String book_id_type = row.getString("book_id_type");
        final String book_id = row.getString("book_id");
        final Set<Author> authors = findAuthorsForBook(book_id_type, book_id);
        final Set<MimeType> formats = findFormatsForBook(book_id_type, book_id);
        final Set<String> keywords = findKeywordsForBook(book_id_type, book_id);

        return new Book(new BookId(BookId.BookIdScheme.valueOf(book_id_type), book_id)
                , row.getString("title")
                , authors
                , keywords
                , formats
        );
    }

    private Set<String> findKeywordsForBook(String bookIdType, String bookId) {
        logger.info("findKeywordsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, keyword from book_key_word where book_id_type = ? and book_id = ?";
        sqlInfo(logger, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::keywordMapper, bookIdType, bookId));
    }

    private String keywordMapper(ResultSet row, int rowNum) throws SQLException {
        return row.getString("keyword");
    }

    private Set<MimeType> findFormatsForBook(String bookIdType, String bookId) {
        logger.info("findFormatsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, format from book_format where book_id_type = ? and book_id = ?";
        sqlInfo(logger, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::formatTypeMapper, bookIdType, bookId));
    }

    private MimeType formatTypeMapper(ResultSet row, int rowNum) throws SQLException {
        final String format = row.getString("format");
        return MimeTypes.toMimeType(format);
    }

    private Set<Author> findAuthorsForBook(String bookIdType, String bookId) {
        logger.info("findAuthorsForBook({}, {})", bookIdType, bookId);

        final String sql = "select author_id, version, name from author where author_id in (select author_id from book_author where book_id_type = ? and book_id = ?)";
        sqlInfo(logger, sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::authorMapper, bookIdType, bookId));
    }

    private Author authorMapper(ResultSet row, int rowNum) throws SQLException {
        final String authorId = row.getString("author_id");
        final String sql = "select name, url from site where author_id = ?";
        sqlInfo(logger, sql, authorId);

        final List<Site> sites = template.query(sql, this::siteMapper, authorId);
        return new Author(AuthorId.withId(authorId),
                row.getTimestamp("version").toInstant(),
                row.getString("name"),
                sites.stream().collect(Collectors.toMap(Site::type, Site::url))
        );
    }

    private Site siteMapper(ResultSet row, int rowNum) throws SQLException {
        try {
            final SiteType type = new SiteType( row.getString("name"));
            final URL url = new URL(row.getString("url"));
            return new Site(type, url);
        } catch (MalformedURLException exc) {
            throw new SQLException("Invalid URL for site " + row, exc);
        }
    }

    @Override
    public Author registerAuthor(String name, Map<SiteType,URL> sites) {
        logger.info("registerAuthor({}, {})", name, sites);

        final AuthorId id = AuthorId.withoutId();
        final Instant version = Instant.now();
        final String sql = "insert into author (author_id, version, name) values (?, ?, ?)";
        sqlInfo(logger, sql, id.uuid(), version, name);

        int count = template.update(sql, id.uuid(), version, name);
        if (count != 1) {
            logger.error("{}: {} {} {}", ServiceError.AUTHOR_NOT_REGISTERED, id.uuid(), version, name);
            ServiceError.AUTHOR_NOT_REGISTERED.raise("Author with new id " + id + " and name " + name);
        }

        for (SiteType type : sites.keySet()) {
            setAuthorSite(id, type, sites.get(type));
        }

        return findAuthorById(id);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, Instant version, String name) {
        logger.info("updateAuthor({}, {})", authorId, name);

        final Timestamp newVersion = Timestamp.from(Instant.now());
        final String sql = "update author set version = ?, name = ? where author_id = ? and version = ?";
        sqlInfo(logger, sql, newVersion, name, authorId.uuid(), version);

        int count = template.update(sql, newVersion, name, authorId.uuid(), version);
        if (count != 1) {
            logger.error("Could not update one author, count is {}, author id is {}, version is {}", count, authorId, version);
            ServiceError.AUTHOR_NOT_UPDATED.raise(authorId + ", " + version + ", '" + name + "'");
        }
        return findAuthorById(authorId);
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        logger.info("forgetAuthor({})", authorId);

        final String sql = "delete from author where author_id = ?";
        sqlInfo(logger, sql, authorId);

        int count = template.update(sql, authorId.uuid());
        if (count != 1) {
            logger.error("{}: {}", ServiceError.AUTHOR_FOR_ID_NOT_FOUND.name(), authorId );
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise();
        }
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        logger.info("setAuthorSite({}, {}, {})", authorId, type, url);

        final String sql = "merge into site (author_id, name, url) values (?, ?, ?)";
        sqlInfo(logger, sql, authorId.uuid(),type.name(),url.toString());

        int count = template.update(sql, authorId.uuid(),type.name(),url.toString());
        if (count != 1) {
            logger.error("{}: {} {}", ServiceError.AUTHOR_SITE_NOT_SET, authorId, type);
            ServiceError.AUTHOR_SITE_NOT_SET.raise(authorId + " " + type);
        }
        return findAuthorById(authorId);
    }

    @Override
    public Book registerBook(BookId bookId, String title, Set<Author> authors, Set<MimeType> formats, Set<String> keywords) {
        logger.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        final String sql = "insert into book (book_id_type, book_id, title) values (?, ?, ?)";
        sqlInfo(logger, sql, bookId.schema().name(), bookId.id(), title);

        int count = template.update(sql, bookId.schema().name(), bookId.id(), title);
        if (count != 1) {
            logger.error("{}: {}, {}, {}", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, title, authors);
            ServiceError.BOOK_NOT_REGISTERED.raise(bookId.toString());
        }

        for (Author author : authors) {
            final String sql2 = "insert into book_author (book_id_type, book_id, author_id) values (?, ?, ?)";
            sqlInfo(logger, sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());
            if (count != 1) {
                logger.error("{}: {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, author);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + author);
            }
        }

        for (MimeType format : formats) {
            final String sql2 = "insert into book_format (book_id_type, book_id, format) values (?, ?, ?)";
            sqlInfo(logger, sql2, bookId.schema().name(), bookId.id(), format.toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), format.toString());
            if (count != 1) {
                logger.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), format);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + format);
            }
        }

        for (String keyword : keywords) {
            final String sql3 = "insert into book_key_word (book_id_type, book_id, keyword) values (?, ?, ?)";
            sqlInfo(logger, sql3, bookId.schema().name(), bookId.id(), keyword);

            count = template.update(sql3, bookId.schema().name(), bookId.id(), keyword);
            if (count != 1) {
                logger.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), keyword);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + keyword);
            }
        }

        return findBookById(bookId);
    }
}

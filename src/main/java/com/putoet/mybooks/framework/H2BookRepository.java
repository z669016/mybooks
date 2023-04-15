package com.putoet.mybooks.framework;

import com.putoet.mybooks.application.port.in.ServiceError;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
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
import java.util.*;
import java.util.stream.Collectors;


/**
 * Class H2BookRepository
 * A read/write repository for book and author data, connected to an H4 database using a Spring JdbcTemplate
 */
@Repository
public class H2BookRepository implements BookRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate template;

    public H2BookRepository(JdbcTemplate template) {
        logger.info("AuthorRepository initialized with JDBC template {}", template.getDataSource());
        this.template = template;
    }

    private void sqlInfo(String sql, Object ... parameters) {
        sql = sql.replace("?", "'{}'");
        logger.info(sql + ";", parameters);
    }

    @Override
    public List<Author> findAuthors() {
        logger.info("findAuthors()");

        final String sql = "select author_id, name from author";
        sqlInfo(sql);

        return template.query(sql, this::authorMapper);
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        logger.info("findAuthorsByName({})", name);
        name = "%" + name.toLowerCase() + "%";

        final String sql = "select author_id, name from author where lower(name) like ?";
        sqlInfo(sql, name);

        return template.query(sql, this::authorMapper, name);
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        logger.info("findAuthorById({})", id);

        try {
            final String sql = "select author_id, name from author where author_id = ?";
            sqlInfo(sql, id.uuid());

            return template.queryForObject(sql, this::authorMapper, id.uuid());
        } catch (EmptyResultDataAccessException exc) {
            logger.warn(exc.getMessage());
        }
        return null;
    }

    @Override
    public List<Book> findBooks() {
        logger.info("findBooks()");

        final String sql = "select book_id_type, book_id, title from book";
        sqlInfo(sql);

        return template.query(sql, this::bookMapper);
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        logger.info("findBooksByTitle({})", title);

        if (title == null || title.isBlank()) {
            logger.error(ServiceError.BOOK_TITLE_REQUIRED.name());
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        }

        title = "%" + title + "%";
        final String sql = "select book_id_type, book_id, title from book where title like ?";
        sqlInfo(sql, title);

        return template.query(sql, this::bookMapper, title);
    }

    @Override
    public Book findBookById(BookId bookId) {
        logger.info("findBookById({})", bookId);

        if (bookId == null) {
            logger.error(ServiceError.BOOK_ID_REQUIRED.name());
            ServiceError.BOOK_ID_REQUIRED.raise();
        }

        final String sql = "select book_id_type, book_id, title from book where book_id_type = ? and book_id = ?";
        sqlInfo(sql, bookId.schema().name(), bookId.id());
        return template.queryForObject(sql, this::bookMapper, bookId.schema().name(), bookId.id());
    }

    @Override
    public List<Book> findBooksByAuthorId(AuthorId authorId) {
        logger.info("findBooksByAuthorId({})", authorId);

        final String sql = "select book_id_type, book_id, title from book where (book_id_type, book_id) in (select book_id_type, book_id from book_author where author_id = ?)";
        sqlInfo(sql, authorId.uuid());

        return template.query(sql, this::bookMapper, authorId.uuid());
    }

    private Book bookMapper(ResultSet row, int rowNum) throws SQLException {
        final String book_id_type = row.getString("book_id_type");
        final String book_id = row.getString("book_id");
        final List<Author> authors = findAuthorsForBook(book_id_type, book_id);
        final List<MimeType> formats = findFormatsForBook(book_id_type, book_id);
        final Set<String> keywords = findKeywordsForBook(book_id_type, book_id);

        return new Book(new BookId(BookId.BookIdScheme.valueOf(book_id_type), book_id)
                , row.getString("title")
                , authors
                , keywords
                , new MimeTypes(formats)
        );
    }

    private Set<String> findKeywordsForBook(String bookIdType, String bookId) {
        logger.info("findKeywordsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, keyword from book_key_word where book_id_type = ? and book_id = ?";
        sqlInfo(sql, bookIdType, bookId);

        return Set.copyOf(template.query(sql, this::keywordMapper, bookIdType, bookId));
    }

    private String keywordMapper(ResultSet row, int rowNum) throws SQLException {
        return row.getString("keyword");
    }

    private List<MimeType> findFormatsForBook(String bookIdType, String bookId) {
        logger.info("findFormatsForBook({}, {})", bookIdType, bookId);

        final String sql = "select book_id_type, book_id, format from book_format where book_id_type = ? and book_id = ?";
        sqlInfo(sql, bookIdType, bookId);

        return template.query(sql, this::formatTypeMapper, bookIdType, bookId);
    }

    private MimeType formatTypeMapper(ResultSet row, int rowNum) throws SQLException {
        final String format = row.getString("format");
        return MimeTypes.toMimeType(format);
    }

    private List<Author> findAuthorsForBook(String bookIdType, String bookId) {
        logger.info("findAuthorsForBook({}, {})", bookIdType, bookId);

        final String sql = "select author_id, name from author where author_id in (select author_id from book_author where book_id_type = ? and book_id = ?)";
        sqlInfo(sql, bookIdType, bookId);

        return template.query(sql, this::authorMapper, bookIdType, bookId);
    }

    private Author authorMapper(ResultSet row, int rowNum) throws SQLException {
        final String authorId = row.getString("author_id");
        final String sql = "select name, url from site where author_id = ?";
        sqlInfo(sql, authorId);

        final List<Site> sites = template.query(sql, this::siteMapper, authorId);
        return new Author(AuthorId.withId(authorId), row.getString("name"),
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
        final String sql = "insert into author (author_id, name) values (?, ?)";
        sqlInfo(sql, id.uuid(), name);

        int count = template.update(sql, id.uuid(), name);
        if (count != 1) {
            logger.error("{}: {} {}", ServiceError.AUTHOR_NOT_REGISTERED, id.uuid(), name);
            ServiceError.AUTHOR_NOT_REGISTERED.raise("Author with new id " + id + " and name " + name);
        }

        for (SiteType type : sites.keySet()) {
            setAuthorSite(id, type, sites.get(type));
        }

        return findAuthorById(id);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, String name) {
        logger.info("updateAuthor({}, {})", authorId, name);

        final String sql = "update author set name = ? where author_id = ?";
        sqlInfo(sql, name, authorId.uuid());

        int count = template.update(sql, name, authorId.uuid());
        if (count != 1) {
            logger.error("Could not update one author, count is {}, author id is {}", count, authorId);
            ServiceError.AUTHOR_NOT_UPDATED.raise(authorId + " " + name);
        }
        return findAuthorById(authorId);
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        logger.info("forgetAuthor({})", authorId);

        final String sql = "delete from author where author_id = ?";
        sqlInfo(sql, authorId);

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
        sqlInfo(sql, authorId.uuid(),type.name(),url.toString());

        int count = template.update(sql, authorId.uuid(),type.name(),url.toString());
        if (count != 1) {
            logger.error("{}: {} {}", ServiceError.AUTHOR_SITE_NOT_SET, authorId, type);
            ServiceError.AUTHOR_SITE_NOT_SET.raise(authorId + " " + type);
        }
        return findAuthorById(authorId);
    }

    @Override
    public Book registerBook(BookId bookId, String title, List<Author> authors, MimeTypes formats, Set<String> keywords) {
        logger.info("registerBook({}, {}, {}, {})", bookId, title, authors, formats);

        final String sql = "insert into book (book_id_type, book_id, title) values (?, ?, ?)";
        sqlInfo(sql, bookId.schema().name(), bookId.id(), title);

        int count = template.update(sql, bookId.schema().name(), bookId.id(), title);
        if (count != 1) {
            logger.error("{}: {}, {}, {}", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, title, authors);
            ServiceError.BOOK_NOT_REGISTERED.raise(bookId.toString());
        }

        for (Author author : authors) {
            final String sql2 = "insert into book_author (book_id_type, book_id, author_id) values (?, ?, ?)";
            sqlInfo(sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), author.id().uuid().toString());
            if (count != 1) {
                logger.error("{}: {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, author);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + author);
            }
        }

        for (MimeType format : formats.mimeTypes()) {
            final String sql2 = "insert into book_format (book_id_type, book_id, format) values (?, ?, ?)";
            sqlInfo(sql2, bookId.schema().name(), bookId.id(), format.toString());

            count = template.update(sql2, bookId.schema().name(), bookId.id(), format.toString());
            if (count != 1) {
                logger.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), format);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + format);
            }
        }

        for (String keyword : keywords) {
            final String sql3 = "insert into book_key_word (book_id_type, book_id, keyword) values (?, ?, ?)";
            sqlInfo(sql3, bookId.schema().name(), bookId.id(), keyword);

            count = template.update(sql3, bookId.schema().name(), bookId.id(), keyword);
            if (count != 1) {
                logger.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), keyword);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + keyword);
            }
        }

        return findBookById(bookId);
    }
}

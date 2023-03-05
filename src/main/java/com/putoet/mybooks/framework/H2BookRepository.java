package com.putoet.mybooks.framework;

import com.putoet.mybooks.application.port.in.ServiceError;
import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class H2BookRepository implements BookRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate template;

    public H2BookRepository(JdbcTemplate template) {
        logger.info("AuthorRepository initialized with JDBC template {}", template.getDataSource());
        this.template = template;
    }

    @Override
    public List<Author> findAuthors() {
        logger.info("findAuthors()");

        return template.query(
                "select author_id, name from author", this::authorMapper);
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        logger.info("findAuthorsByName({})", name);

        return template.query(
                "select author_id, name from author where lower(name) like ?",
                this::authorMapper, "%" + name.toLowerCase() + "%");
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        logger.info("AuthorId({})", id);

        try {
            return template.queryForObject(
                    "select author_id, name from author where author_id = ?",
                    this::authorMapper, id.uuid());
        } catch (EmptyResultDataAccessException exc) {
            logger.warn(exc.getMessage());
        }
        return null;
    }

    @Override
    public List<Book> findBooks() {
        logger.info("findBooks()");

        return template.query(
                "select book_id_type, book_id, title, description from book",
                this::bookMapper);
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        logger.info("findBooksByTitle({})", title);

        if (title == null || title.isBlank()) {
            logger.error(ServiceError.BOOK_TITLE_REQUIRED.name());
            ServiceError.BOOK_TITLE_REQUIRED.raise();
        }

        throw new UnsupportedOperationException("findBooksByTitle");
    }

    @Override
    public Book findBookById(BookId bookId) {
        logger.info("findBookById({})", bookId);

        if (bookId == null) {
            logger.error(ServiceError.BOOK_ID_REQUIRED.name());
            ServiceError.BOOK_ID_REQUIRED.raise();
        }

        return template.queryForObject("select book_id_type, book_id, title, description from book where book_id_type = ? and book_id = ?",
                this::bookMapper, bookId.schema().name(), bookId.id());
    }

    @Override
    public List<Book> findBooksByAuthorId(AuthorId authorId) {
        logger.info("findBooksByAuthorId({})", authorId);

        return template.query("select book_id_type, book_id, title, description from book where (book_id_type, book_id) in (select book_id_type, book_id from book_author where author_id = ?)",
                this::bookMapper, authorId.uuid());
    }

    private Book bookMapper(ResultSet row, int rowNum) throws SQLException {
        final String book_id_type = row.getString("book_id_type");
        final String book_id = row.getString("book_id");
        final List<Author> authors = findAuthorsForBook(book_id_type, book_id);
        final List<FormatType> formats = findFormatsForBook(book_id_type, book_id);

        return new Book(new BookId(BookId.BookIdScheme.valueOf(book_id_type), book_id)
                , row.getString("title")
                , authors
                , row.getString("description")
                , List.of()
                , formats
        );
    }

    private List<FormatType> findFormatsForBook(String bookIdType, String bookId) {
        logger.info("findFormatsForBook({}, {})", bookIdType, bookId);

        return template.query(
                "select book_id_type, book_id, format from book_format where book_id_type = ? and book_id = ?",
                this::formatTypeMapper, bookIdType, bookId);
    }

    private FormatType formatTypeMapper(ResultSet row, int rowNum) throws SQLException {
        final String format = row.getString("format");
        return FormatType.valueOf(format);
    }

    private List<Author> findAuthorsForBook(String bookIdType, String bookId) {
        logger.info("findAuthorsForBook({}, {})", bookIdType, bookId);

        return template.query(
                "select author_id, name from author where author_id in (select author_id from book_author where book_id_type = ? and book_id = ?)",
                this::authorMapper, bookIdType, bookId);
    }

    private Author authorMapper(ResultSet row, int rowNum) throws SQLException {
        final String authorId = row.getString("author_id");
        final List<Site> sites = template.query(
                "select name, url from site where author_id = ?",
                this::siteMapper, authorId);

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
        int count = template.update("insert into author (author_id, name) values (?, ?)", id.uuid(), name);
        if (count != 1)
            ServiceError.AUTHOR_NOT_REGISTERED.raise();

        for (SiteType type : sites.keySet()) {
            setAuthorSite(id, type, sites.get(type));
        }

        return findAuthorById(id);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, String name) {
        logger.info("updateAuthor({}, {})", authorId, name);

        int count = template.update("update author set name = ? where author_id = ?", name, authorId.uuid());
        return findAuthorById(authorId);
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        logger.info("forgetAuthor({})", authorId);

        int count = template.update("delete from author where author_id = ?", authorId.uuid());
        if (count != 1) {
            logger.error("{}: {}", ServiceError.AUTHOR_FOR_ID_NOT_FOUND.name(), authorId );
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise();
        }
    }

    @Override
    public Author setAuthorSite(AuthorId authorId, SiteType type, URL url) {
        logger.info("setAuthorSite({}, {}, {})", authorId, type, url);

        int count = template.update("merge into site (author_id, name, url) values (?, ?, ?)", authorId.uuid(),type.name(),url.toString());

        if (count != 1) {
            logger.error("{}: {} {}", ServiceError.AUTHOR_SITE_NOT_SET, authorId, type);
            ServiceError.AUTHOR_SITE_NOT_SET.raise(authorId + " " + type);
        }
        return findAuthorById(authorId);
    }

    @Override
    public Book registerBook(BookId bookId, String title, List<Author> authors, String description, List<FormatType> formats) {
        logger.info("registerBook({}, {}, {}, {}, {})", bookId, title, authors, description, formats);

        int count = template.update("insert into book (book_id_type, book_id, title, description) values (?, ?, ?, ?)",
                bookId.schema().name(), bookId.id(), title, description);
        if (count != 1) {
            logger.error("{}: {}, {}, {}, {}", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, title, authors, description);
            ServiceError.BOOK_NOT_REGISTERED.raise(bookId.toString());
        }

        for (Author author : authors) {
            count = template.update("insert into book_author (book_id_type, book_id, author_id) values (?, ?, ?)",
                    bookId.schema().name(), bookId.id(), author.id().uuid().toString());
            if (count != 1) {
                logger.error("{}: {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId, author);
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + author);
            }
        }

        for (FormatType format : formats) {
            count = template.update("insert into book_format (book_id_type, book_id, format) values (?, ?, ?)",
                    bookId.schema().name(), bookId.id(), format.name());
            if (count != 1) {
                logger.error("{}: {}, {}, {})", ServiceError.BOOK_NOT_REGISTERED.name(), bookId.schema(), bookId.id(), format.name());
                ServiceError.BOOK_NOT_REGISTERED.raise(bookId + " " + format);
            }
        }

        return findBookById(bookId);
    }
}

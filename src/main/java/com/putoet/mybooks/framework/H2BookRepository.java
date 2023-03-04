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
        logger.info("AuthorRepository initialized with JDBC template " + template.getDataSource());
        this.template = template;
    }

    @Override
    public List<Author> findAuthors() {
        return template.query(
                "select id, name from author", this::authorMapper);
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        return template.query(
                "select id, name from author where lower(name) like ?",
                this::authorMapper, "%" + name.toLowerCase() + "%");
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        try {
            return template.queryForObject(
                    "select id, name from author where id = ?",
                    this::authorMapper, id.uuid());
        } catch (EmptyResultDataAccessException ignored) {}
        return null;
    }

    @Override
    public List<Book> findBooks() {
        return template.query(
                "select id_type, id, title, description from book",
                this::bookMapper);
    }

    @Override
    public List<Book> findBooksByTitle(String title) {
        throw new UnsupportedOperationException("findBooksByTitle");
    }

    @Override
    public Book findBookById(BookId bookId) {
        throw new UnsupportedOperationException("findBookById");
    }

    @Override
    public List<Book> findBooksByAuthorId(AuthorId authorId) {
        throw new UnsupportedOperationException("findBooksByAuthorId");
    }

    private Book bookMapper(ResultSet row, int rowNum) throws SQLException {
        final String book_id_type = row.getString("id_type");
        final String book_id = row.getString("id");
        final List<Author> authors = findAuthorsForBook(book_id_type, book_id);

        return new Book(new BookId(BookId.BookIdScheme.valueOf(book_id_type), book_id)
                , row.getString("title")
                , authors
                , ""
                , List.of()
                , List.of()
        );
    }

    private List<Author> findAuthorsForBook(String bookIdType, String bookId) {
        return template.query(
                "select id, name from author where id in (select author_id from book_author where book_id_type = ? and book_id = ?)",
                this::authorMapper, bookIdType, bookId);
    }

    private Author authorMapper(ResultSet row, int rowNum) throws SQLException {
        final String authorId = row.getString("id");
        final List<Site> sites = template.query(
                "select name, url from site where author_id=?",
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
    public Author createAuthor(String name, Map<SiteType,URL> sites) {
        final AuthorId id = AuthorId.withoutId();
        int count = template.update("insert into author values (?, ?)", id.uuid(), name);
        if (count != 1)
            ServiceError.AUTHOR_NOT_CREATED.raise();

        for (SiteType type : sites.keySet()) {
            setAuthorSite(id, type, sites.get(type));
        }

        return findAuthorById(id);
    }

    @Override
    public Author updateAuthor(AuthorId authorId, String name) {
        int count = template.update("update author set name = ? where id = ?", name, authorId.uuid());
        return findAuthorById(authorId);
    }

    @Override
    public void forgetAuthor(AuthorId authorId) {
        int count = template.update("delete author where id = ?", authorId.uuid());
        if (count != 1)
            ServiceError.AUTHOR_FOR_ID_NOT_FOUND.raise();
    }

    @Override
    public Author setAuthorSite(AuthorId id, SiteType type, URL url) {
        int count = template.update("merge into site values (?, ?, ?)", id.uuid(),type.name(),url.toString());

        if (count != 1)
            ServiceError.AUTHOR_SITE_NOT_SET.raise(id.toString() + " " + type);

        return findAuthorById(id);
    }
}

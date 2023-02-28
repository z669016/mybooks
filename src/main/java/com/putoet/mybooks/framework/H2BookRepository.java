package com.putoet.mybooks.framework;

import com.putoet.mybooks.application.port.out.BookRepository;
import com.putoet.mybooks.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
        return template.queryForObject(
                "select id, name from author where id=?",
                this::authorMapper, id.uuid());
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
                "select id, name, url from site where author_id=?",
                this::siteMapper, authorId);

        return new Author(AuthorId.withId(row.getString("id")),
                row.getString("name"),
                sites.stream().collect(Collectors.toMap(Site::type, site -> site))
        );
    }

    private Site siteMapper(ResultSet row, int rowNum) throws SQLException {
        final SiteId id = SiteId.withId(row.getString("id"));
        try {
            final SiteType type = SiteType.OTHER( row.getString("name"));
            final URL url = new URL(row.getString("url"));
            return new Site(id,type, url);
        } catch (MalformedURLException exc) {
            throw new SQLException("Invalid URL for site with id '" + id + "'", exc);
        }
    }

    @Override
    public Author createAuthor(Author author) {
        int count = template.update("insert into author values (?, ?)", author.id().uuid(), author.name());
        if (count == 1) {
            for (Site site : author.sites().values()) {
                count = template.update("insert into site values (?, ?, ?, ?)",
                        site.id().uuid(), author.id().uuid(), site.type().name(), site.url().toString());
                if (count != 1)
                    throw new IllegalArgumentException("Could not insert author: " + author);
            }
        }

        return author;
    }
}

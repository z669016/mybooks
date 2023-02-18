package com.putoet.mybooks.framework;

import com.putoet.mybooks.application.port.out.AuthorRepository;
import com.putoet.mybooks.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Component("authorRepository")
public class H2AuthorRepository implements AuthorRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate template;

    public H2AuthorRepository(JdbcTemplate template) {
        logger.info("AuthorRepository initialized with JDBC template " + template.getDataSource());
        this.template = template;
    }

    @Override
    public List<Author> findAuthorByName(String name) {
        return template.query(
                "select id, name from author where name like ?",
                this::authorMapper, name);
    }

    @Override
    public Author findAuthorById(AuthorId id) {
        return template.queryForObject(
                "select id, name from author where id=?",
                this::authorMapper, id);
    }

    @Override
    public Author persist(Author author) {
        return null;
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
}

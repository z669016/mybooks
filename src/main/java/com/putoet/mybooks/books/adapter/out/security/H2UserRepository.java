package com.putoet.mybooks.books.adapter.out.security;

import com.putoet.mybooks.books.application.port.in.security.SecurityError;
import com.putoet.mybooks.books.application.port.out.security.UserPort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.putoet.mybooks.books.adapter.out.persistence.SqlUtil.sqlInfo;

@Repository
public class H2UserRepository implements UserPort {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate template;

    public H2UserRepository(JdbcTemplate template) {
        logger.info("UserRepository initialized with JDBC template {}", template.getDataSource());
        this.template = template;
    }

    @Override
    public List<User> findUsers() {
        logger.info("findUsers()");

        final String sql = "select id, name, password, access from users";
        sqlInfo(logger, sql);

        return template.query(sql, this::userMapper);
    }

    @Override
    public User findUserById(String id) {
        logger.info("findUserById({})", id);

        final String sql = "select id, name, password, access from users where id = ?";
        sqlInfo(logger, sql, id);

        final User user = template.queryForObject(sql, this::userMapper, id);
        if (user == null)
            SecurityError.USER_ID_INVALID.raise(id);

        return user;
    }

    private User userMapper(ResultSet resultSet, int i) throws SQLException {
        final String id = resultSet.getString("id");
        final String name = resultSet.getString("name");
        final String password = resultSet.getString("password");
        final AccessRole accessRole = AccessRole.from(resultSet.getString("access"));
        return new User(id, name, password, accessRole);
    }

    @Override
    public void forgetUser(String id) {
        logger.info("forgetUser({})", id);

        final String sql = "delete from users where id = ?";
        sqlInfo(logger, sql, id);

        int count = template.update(sql, id);
        if (count != 1) {
            logger.error("{}: {}", SecurityError.USER_ID_INVALID.name(), id);
            SecurityError.USER_ID_INVALID.raise(id);
        }
    }

    @Override
    public User registerUser(String id, String name, String password, AccessRole accessRole) {
        logger.info("registerUser({}, {}, {}, {})", id, name, password, accessRole);

        final String sql = "insert into users (id, name, password, access) values (?, ?, ?, ?)";
        sqlInfo(logger, sql, id, name, password, accessRole);

        int count = template.update(sql, id, name, password, accessRole.name());
        if (count != 1) {
            logger.error("{}: {} {} '{}' {}", SecurityError.USER_REGISTRATION_ERROR, id, name, password, accessRole);
            SecurityError.USER_REGISTRATION_ERROR.raise("User with new id " + id + ", name " + name + ", and accessRole " + accessRole);
        }

        return findUserById(id);
    }
}

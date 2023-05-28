package com.putoet.mybooks.books.adapter.out.security;

import com.putoet.mybooks.books.application.port.in.security.UserError;
import com.putoet.mybooks.books.application.port.out.security.UserPersistencePort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

import static com.putoet.mybooks.books.adapter.out.persistence.SqlUtil.sqlInfo;

@Repository
@Slf4j
@RequiredArgsConstructor
public class H2UserRepository implements UserPersistencePort {

    private final JdbcTemplate template;

    @SneakyThrows
    @Override
    public String toString() {
        return String.format("%s(%s)", this.getClass().getName(),
                Objects.requireNonNull(template.getDataSource()).getConnection().getMetaData().getURL());
    }

    @Override
    public Set<User> findUsers() {
        log.info("findUsers()");

        final String sql = "select id, name, password, access from users";
        sqlInfo(log, sql);

        return Set.copyOf(template.query(sql, this::userMapper));
    }

    @Override
    public User findUserById(String id) {
        log.info("findUserById({})", id);

        try {
            final String sql = "select id, name, password, access from users where id = ?";
            sqlInfo(log, sql, id);

            return template.queryForObject(sql, this::userMapper, id);
        } catch (EmptyResultDataAccessException exc) {
            log.warn(exc.getMessage());
        }
        return null;
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
        log.info("forgetUser({})", id);

        final String sql = "delete from users where id = ?";
        sqlInfo(log, sql, id);

        int count = template.update(sql, id);
        if (count != 1) {
            log.error("{}: {}", UserError.USER_ID_INVALID.name(), id);
            UserError.USER_ID_INVALID.raise(id);
        }
    }

    @Override
    public User registerUser(String id, String name, String password, AccessRole accessRole) {
        log.info("registerUser({}, {}, {}, {})", id, name, password, accessRole);

        final String sql = "insert into users (id, name, password, access) values (?, ?, ?, ?)";
        sqlInfo(log, sql, id, name, password, accessRole);

        int count = template.update(sql, id, name, password, accessRole.name());
        if (count != 1) {
            log.error("{}: {} {} '{}' {}", UserError.USER_REGISTRATION_ERROR, id, name, password, accessRole);
            UserError.USER_REGISTRATION_ERROR.raise("User with new id " + id + ", name " + name + ", and accessRole " + accessRole);
        }

        return findUserById(id);
    }
}

package com.putoet.mybooks.books.application.security;

import com.putoet.mybooks.books.application.port.in.security.UserError;
import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import com.putoet.mybooks.books.application.port.out.security.UserPersistencePort;
import com.putoet.mybooks.books.application.security.event.UserCreatedSecurityEvent;
import com.putoet.mybooks.books.application.security.event.UserDeletedSecurityEvent;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@ToString
public class UserService implements UserManagementPort {
    // Regular Expression by RFC 5322 for Email Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void forgetUser(String id) {
        log.info("forgetUser({})", id);

        if (id == null || id.isBlank())
            throw UserError.USER_ID_REQUIRED.exception();

        userPersistencePort.forgetUser(id);
        applicationEventPublisher.publishEvent(new UserDeletedSecurityEvent(this, id));
    }

    @Override
    public User registerUser(String id, String name, String password, AccessRole accessRole) {
        if (id == null || id.isBlank() || !EMAIL_PATTERN.matcher(id).matches())
            throw UserError.USER_ID_INVALID.exception(id);

        if (name == null || name.isBlank())
            throw UserError.USER_NAME_REQUIRED.exception();

        if (password == null || password.isBlank())
            throw UserError.USER_PASSWORD_REQUIRED.exception();

        if (password.length() < 8)
            throw UserError.USER_PASSWORD_TOO_SIMPLE.exception();

        if (accessRole == null)
            throw UserError.USER_ACCESS_ROLE_REQUIRED.exception();

        final var user = userPersistencePort.registerUser(id, name, passwordEncoder.encode(password), accessRole);
        if (user == null)
            throw UserError.USER_REGISTRATION_ERROR.exception();

        applicationEventPublisher.publishEvent(new UserCreatedSecurityEvent(this, id));
        return user;
    }

    @Override
    public Optional<User> userById(String id) {
        log.info("userById({})", id);

        if (id == null || id.isBlank())
            throw UserError.USER_ID_REQUIRED.exception();

        return Optional.ofNullable(userPersistencePort.findUserById(id));
    }

    @Override
    public Set<User> users() {
        log.info("users()");

        return userPersistencePort.findUsers();
    }

    @Bean
    public UserDetailsService userDetailsService(UserPersistencePort userPort) {
        return id -> {
            final var user = userPort.findUserById(id);
            if (user == null)
                throw new UsernameNotFoundException(id);

            return new UserDetails() {
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of(new SimpleGrantedAuthority("ROLE_" + user.accessRole().name()));
                }

                @Override
                public String getPassword() {
                    return user.password();
                }

                @Override
                public String getUsername() {
                    return user.id();
                }
            };
        };
    }
}

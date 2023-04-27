package com.putoet.mybooks.books.application.port.security;

import com.putoet.mybooks.books.application.port.in.security.*;
import com.putoet.mybooks.books.application.port.out.security.UserPort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service("userService")
public class UserService implements Users, UserById, ForgetUser, RegisterUser {
    // Regular Expression by RFC 5322 for Email Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserPort userPort, PasswordEncoder passwordEncoder) {
        logger.info("SecurityService({}, {})", userPort, passwordEncoder);
        this.userPort = userPort;
        this.passwordEncoder = passwordEncoder;

        userPort.findUsers().forEach(System.out::println);
    }

    @Override
    public void forgetUser(String id) {
        logger.info("forgetUser({})", id);

        if (id == null || id.isBlank())
            SecurityError.USER_ID_REQUIRED.raise();

        userPort.forgetUser(id);
    }

    @Override
    public User registerUser(String id, String name, String password, AccessRole accessRole) {
        if (id == null || id.isBlank() || !EMAIL_PATTERN.matcher(id).matches())
            SecurityError.USER_ID_INVALID.raise(id);

        if (name == null || name.isBlank())
            SecurityError.USER_NAME_REQUIRED.raise();

        if (password == null || password.isBlank())
            SecurityError.USER_PASSWORD_REQUIRED.raise();

        if (password.length() < 8)
            SecurityError.USER_PASSWORD_TOO_SIMPLE.raise();

        if (accessRole == null)
            SecurityError.USER_ACCESS_ROLE_REQUIRED.raise();

        return userPort.registerUser(id, name, passwordEncoder.encode(password), accessRole);
    }

    @Override
    public Optional<User> userById(String id) {
        logger.info("userById({})", id);

        if (id == null || id.isBlank())
            SecurityError.USER_ID_REQUIRED.raise();

        return Optional.ofNullable(userPort.findUserById(id));
    }

    @Override
    public List<User> users() {
        logger.info("users()");

        return userPort.findUsers();
    }

    @Bean
    public UserDetailsService userDetailsService(UserPort userPort) {
        return id -> {
            final User user = userPort.findUserById(id);
            if (user == null)
                throw new UsernameNotFoundException(id);

            return new UserDetails() {
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of((GrantedAuthority) () -> user.accessRole().name());
                }

                @Override
                public String getPassword() {
                    return user.password();
                }

                @Override
                public String getUsername() {
                    return user.id();
                }

                @Override
                public boolean isAccountNonExpired() {
                    return true;
                }

                @Override
                public boolean isAccountNonLocked() {
                    return true;
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return true;
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }
            };
        };
    }
}

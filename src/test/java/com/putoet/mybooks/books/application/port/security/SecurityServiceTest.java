package com.putoet.mybooks.books.application.port.security;

import com.putoet.mybooks.books.application.port.in.security.SecurityException;
import com.putoet.mybooks.books.application.port.out.security.UserPort;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SecurityServiceTest {
    private UserPort userPort;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    private final User ADMIN = new User("z669016@gmail.com", "Z669016", "1password!", AccessRole.ADMIN);
    private final User USER = new User("putoet@outlook.com", "PUTOET", "2password!", AccessRole.USER);

    @BeforeEach
    void setup() {
        userPort = mock(UserPort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userPort, passwordEncoder);
    }

    @Test
    void forgetUser() {
        assertThrows(SecurityException.class, () -> userService.forgetUser(null));
        assertThrows(SecurityException.class, () -> userService.forgetUser("   "));

        userService.forgetUser(USER.id());
        verify(userPort).forgetUser(USER.id());
    }

    @Test
    void registerUser() {
        assertThrows(SecurityException.class, () -> userService.registerUser(null, null, null, null));
        assertThrows(SecurityException.class, () -> userService.registerUser("   ", null, null, null));
        assertThrows(SecurityException.class, () -> userService.registerUser(USER.id(), null, null, null));
        assertThrows(SecurityException.class, () -> userService.registerUser(USER.id(), "  ", null, null));
        assertThrows(SecurityException.class, () -> userService.registerUser(USER.id(), USER.name(), null, null));
        assertThrows(SecurityException.class, () -> userService.registerUser(USER.id(), USER.name(), "  ", null));
        assertThrows(SecurityException.class, () -> userService.registerUser(USER.id(), USER.name(), "2password!", null));

        final String password = "encoded_password";
        when(passwordEncoder.encode(USER.password())).thenReturn(password);
        userService.registerUser(USER.id(), USER.name(), USER.password(), USER.accessRole());

        verify(passwordEncoder).encode(USER.password());
        verify(userPort).registerUser(USER.id(), USER.name(), password, USER.accessRole());
    }

    @Test
    void userById() {
        assertThrows(SecurityException.class, () -> userService.userById(null));
        assertThrows(SecurityException.class, () -> userService.userById("   "));

        userService.userById(USER.id());
        verify(userPort).findUserById(USER.id());
    }

    @Test
    void users() {
        userService.users();
        verify(userPort).findUsers();
    }

    @Test
    void passwordEncoder() {
        final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        printPassword(ADMIN.password(), passwordEncoder);
        printPassword(ADMIN.password(), passwordEncoder);
        printPassword(ADMIN.password(), passwordEncoder);
        printPassword(ADMIN.password(), passwordEncoder);
        printPassword(USER.password(), passwordEncoder);
        printPassword(USER.password(), passwordEncoder);
        printPassword(USER.password(), passwordEncoder);
        printPassword(USER.password(), passwordEncoder);
    }

    private void printPassword(String password, PasswordEncoder passwordEncoder) {
        final String encoded = passwordEncoder.encode(password);
        final boolean matches = passwordEncoder.matches(password, encoded);
        System.out.printf("'%s' encoded is '%s', matches = %b%n", password, encoded, matches);
    }
}
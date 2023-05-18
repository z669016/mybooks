package com.putoet.mybooks.books.application.port.security;

import com.putoet.mybooks.books.application.port.in.security.UserException;
import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import com.putoet.mybooks.books.application.port.out.security.UserPersistencePort;
import com.putoet.mybooks.books.application.security.UserService;
import com.putoet.mybooks.books.domain.security.AccessRole;
import com.putoet.mybooks.books.domain.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserPersistencePort userPort;
    private PasswordEncoder passwordEncoder;
    private UserManagementPort userManagementPort;

    private final User ADMIN = new User("z669016@gmail.com", "Z669016", "1password!", AccessRole.ADMIN);
    private final User USER = new User("putoet@outlook.com", "PUTOET", "2password!", AccessRole.USER);

    @BeforeEach
    void setup() {
        userPort = mock(UserPersistencePort.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userManagementPort = new UserService(userPort, passwordEncoder);
    }

    @Test
    void forgetUser() {
        userManagementPort.forgetUser(USER.id());

        assertAll(
                () -> verify(userPort).forgetUser(USER.id()),

                // error conditions
                () -> assertThrows(UserException.class, () -> userManagementPort.forgetUser(null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.forgetUser("   "))
        );

    }

    @Test
    void registerUser() {
        final String password = "encoded_password";
        when(passwordEncoder.encode(USER.password())).thenReturn(password);
        userManagementPort.registerUser(USER.id(), USER.name(), USER.password(), USER.accessRole());

        assertAll(
                () -> verify(passwordEncoder).encode(USER.password()),
                () -> verify(userPort).registerUser(USER.id(), USER.name(), password, USER.accessRole()),

                // error conditions
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(null, null, null, null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser("   ", null, null, null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(USER.id(), null, null, null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(USER.id(), "  ", null, null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(USER.id(), USER.name(), null, null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(USER.id(), USER.name(), "  ", null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.registerUser(USER.id(), USER.name(), "2password!", null))
        );
    }

    @Test
    void userById() {
        userManagementPort.userById(USER.id());
        assertAll(
                () -> verify(userPort).findUserById(USER.id()),

                // error conditions
                () -> assertThrows(UserException.class, () -> userManagementPort.userById(null)),
                () -> assertThrows(UserException.class, () -> userManagementPort.userById("   "))
        );
    }

    @Test
    void users() {
        userManagementPort.users();
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
package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.SiteType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAuthorCommandTest {
    public static final String name = "M. Name";

    @Test
    void constructor() {
        assertThrows(RegisterAuthor.RegisterAuthorError.class, () -> new RegisterAuthorCommand(null, null));
        assertThrows(RegisterAuthor.RegisterAuthorError.class, () -> new RegisterAuthorCommand(name, null));
    }

    @Test
    void withName() {
        final RegisterAuthorCommand command = RegisterAuthorCommand.withName(name).build();
        assertEquals(name, command.name());
        assertTrue(command.sites().isEmpty());
    }

    @Test
    void withNameAndSites() {
        final RegisterAuthorCommand command = RegisterAuthorCommand
                .withName(name)
                .withSite("NOS", "https://nos.nl")
                .withSite(new SiteType("NU.NL"), "https://nu.nl")
                .build();

        assertEquals(name, command.name());
        assertEquals(2, command.sites().size());

        System.out.println(command);
    }
}
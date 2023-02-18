package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.SiteType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegisterAuthorCommandTest {
    public static final String name = "M. Name";

    @Test
    void constructor() {
        assertThrows(NullPointerException.class, () -> new RegisterAuthorCommand(null, null));
        assertThrows(NullPointerException.class, () -> new RegisterAuthorCommand(name, null));

        assertThrows(IllegalArgumentException.class, () -> new RegisterAuthorCommand(" ", Map.of()));
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
                .withSite(SiteType.OTHER("NU.NL"), "https://nu.nl")
                .build();

        assertEquals(name, command.name());
        assertEquals(2, command.sites().size());

        System.out.println(command);
    }
}
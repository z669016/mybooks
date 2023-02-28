package com.putoet.mybooks.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BookTest {
    private static final BookId id = new BookId(BookId.BookIdScheme.ISBN, "978-1-83921-196-6");
    private static final String title = "Get Your Hands Dirty on Clean Architecture";
    private static final Author author = mock(Author.class);
    private static final List<Author> authors = List.of(author);
    private static final String description = """
            We would all like to build software architecture that yields adaptable and flexible software with low development costs. But, unreasonable deadlines and shortcuts make it very hard to create such an architecture.
                                
            Get Your Hands Dirty on Clean Architecture starts with a discussion about the conventional layered architecture style and its disadvantages. It also talks about the advantages of the domain-centric architecture styles of Robert C. Martin's Clean Architecture and Alistair Cockburn's Hexagonal Architecture. Then, the book dives into hands-on chapters that show you how to manifest a hexagonal architecture in actual code. You'll learn in detail about different mapping strategies between the layers of a hexagonal architecture and see how to assemble the architecture elements into an application. The later chapters demonstrate how to enforce architecture boundaries. You'll also learn what shortcuts produce what types of technical debt and how, sometimes, it is a good idea to willingly take on those debts.
                                
            After reading this book, you'll have all the knowledge you need to create applications using the hexagonal architecture style of web development.""";
    private static final List<String> keywords = List.of("architecture", "adapters", "ports");
    private static final List<FormatType> formats = List.of(FormatType.EPUB);
    private static final Book book = new Book(id, title, authors, description, keywords, formats);

    @Test
    void constructor() {
        // Null values are not allowed for any attribute
        assertThrows(NullPointerException.class, () -> new Book(null,null, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, null, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, null, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, title, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, title, authors, null, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, title, authors, description, null, null));
        assertThrows(NullPointerException.class, () -> new Book(id, title, authors, description, keywords, null));

        // Title and list of authors may not be empty
        assertThrows(IllegalArgumentException.class, () -> new Book(id, "", authors, description, keywords, formats));
        assertThrows(IllegalArgumentException.class, () -> new Book(id, " ", authors, description, keywords, formats));
        assertThrows(IllegalArgumentException.class, () -> new Book(id, title, List.of(), description, keywords, formats));


        // Description, formats and keywords may be empty
        new Book(id, title, authors, "", keywords, formats);
        new Book(id, title, authors, description, List.of(), formats);
        new Book(id, title, authors, description, keywords, List.of());

        // correctly constructed book
        new Book(id, title, authors, description, keywords, formats);
    }

    @Test
    void addFormat() {
        final var updated = book.addFormat(FormatType.PDF);

        assertNotEquals(book, updated);
        assertEquals(2, updated.formats().size());
        assertTrue(updated.formats().contains(FormatType.PDF));

        assertThrows(IllegalArgumentException.class, () -> updated.addFormat(FormatType.PDF));
    }

    @Test
    void addKeyword() {
        assertThrows(NullPointerException.class, () -> book.addKeyword(null));
        assertThrows(IllegalArgumentException.class, () -> book.addKeyword(" "));

        final var updated = book.addKeyword(" Hexagonal");

        assertNotEquals(book, updated);
        assertEquals(4, updated.keywords().size());
        assertTrue(updated.keywords().contains("hexagonal"));

        assertThrows(IllegalArgumentException.class, () -> updated.addKeyword("hexagonal"));
    }

    @Test
    void addAuthor() {
        final var me = new Author(AuthorId.withoutId(), "My Name", Map.of());
        final var updated = book.addAuthor(me);

        assertNotEquals(book, updated);
        assertEquals(2, updated.authors().size());
        assertTrue(updated.authors().contains(me));

        assertThrows(IllegalArgumentException.class, () -> updated.addAuthor(me));
    }

    @Test
    void setDescription() {
        final String description = "New description";
        final var updated = book.description(description);

        assertNotEquals(book, updated);
        assertEquals(description, updated.description());
    }
}
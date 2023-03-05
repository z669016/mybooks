package com.putoet.mybooks;

import com.putoet.mybooks.application.BookInquiryService;
import com.putoet.mybooks.application.BookService;
import com.putoet.mybooks.domain.Author;
import com.putoet.mybooks.domain.Book;
import com.putoet.mybooks.framework.FolderRepository;
import com.putoet.mybooks.framework.H2BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class MybooksApplicationTests {
	private static final String LEANPUB = "/Users/renevanputten/OneDrive/Documents/Books/leanpub";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void loadBooks() {
		final H2BookRepository database = new H2BookRepository(jdbcTemplate);
		final FolderRepository folder = new FolderRepository(Paths.get(LEANPUB));

		final BookInquiryService inquiry = new BookInquiryService(folder);
		final BookService service = new BookService(database);

		final Map<String,Author> storedAuthors = new HashMap<>();
		for (Author author : inquiry.authors()) {
			storedAuthors.put(author.name(), service.registerAuthor(author.name(), author.sites()));
			System.out.println(storedAuthors.get(author.name()));
		}

		final List<Book> books = inquiry.books();
		for (Book book : books) {
			final List<Author> authors = book.authors().stream()
							.map(author -> storedAuthors.get(author.name()))
									.toList();

			service.registerBook(book.id(), book.title(), authors, book.description(), book.formats());
		}

		System.out.println("All stored books:");
		service.books().forEach(book -> System.out.println(book.id()));
	}

}

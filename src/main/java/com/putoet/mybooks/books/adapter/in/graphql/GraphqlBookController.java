package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.domain.BookId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
public class GraphqlBookController {
    public static final Logger log = LoggerFactory.getLogger(GraphqlBookController.class);

    private final BookManagementInquiryPort bookManagementInquiryPort;

    public GraphqlBookController(final BookManagementInquiryPort bookManagementInquiryPort) {
        log.info("GraphqlBookController({})", bookManagementInquiryPort);

        this.bookManagementInquiryPort = bookManagementInquiryPort;
    }

    @QueryMapping
    public Collection<GraphqlBookResponse> books() {
        final var books = bookManagementInquiryPort.books();
        log.debug("books: {}", books);
        return GraphqlBookResponse.from(books);
    }

    @QueryMapping
    public Collection<GraphqlBookResponse> booksByTitle(@Argument String title) {
        final var books = bookManagementInquiryPort.booksByTitle(title);
        log.debug("books: {}", books);
        return GraphqlBookResponse.from(books);
    }

    @QueryMapping
    public Collection<GraphqlBookResponse> booksByAuthorName(@Argument String name) {
        final var books = bookManagementInquiryPort.booksByAuthorName(name);
        log.debug("books: {}", books);
        return GraphqlBookResponse.from(books);
    }

    @QueryMapping
    public GraphqlBookResponse bookById(@Argument String schema, @Argument String id) {
        final var bookId = new BookId(schema, id);
        final var book = bookManagementInquiryPort.bookById(bookId);
        log.debug("book: {}", book);
        return book.map(GraphqlBookResponse::from).orElseThrow(() -> new NotFoundException(bookId.toString()));
    }
}

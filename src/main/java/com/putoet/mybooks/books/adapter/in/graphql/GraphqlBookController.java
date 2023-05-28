package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.domain.BookId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GraphqlBookController {
    private final BookManagementInquiryPort bookManagementInquiryPort;

    @QueryMapping
    public Collection<GraphqlBookResponse> books() {
        return GraphqlBookResponse.from(bookManagementInquiryPort.books());
    }

    @QueryMapping
    public Collection<GraphqlBookResponse> booksByTitle(@Argument String title) {
        return GraphqlBookResponse.from(bookManagementInquiryPort.booksByTitle(title));
    }

    @QueryMapping
    public Collection<GraphqlBookResponse> booksByAuthorName(@Argument String name) {
        return GraphqlBookResponse.from(bookManagementInquiryPort.booksByAuthorName(name));
    }

    @QueryMapping
    public GraphqlBookResponse bookById(@Argument String schema, @Argument String id) {
        final var bookId = new BookId(schema, id);
        final var book = bookManagementInquiryPort.bookById(bookId);
        return book.map(GraphqlBookResponse::from).orElseThrow(() -> new NotFoundException(bookId.toString()));
    }
}

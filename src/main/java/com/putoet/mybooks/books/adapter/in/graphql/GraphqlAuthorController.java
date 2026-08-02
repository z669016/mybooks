package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.domain.AuthorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
public class GraphqlAuthorController {
    private static final Logger log = LoggerFactory.getLogger(GraphqlAuthorController.class);

    private final BookManagementInquiryPort bookManagementInquiryPort;

    public GraphqlAuthorController(BookManagementInquiryPort bookManagementInquiryPort) {
        log.info("GraphqlAuthorController({})", bookManagementInquiryPort);
        this.bookManagementInquiryPort = bookManagementInquiryPort;
    }

    @QueryMapping
    public Collection<GraphqlAuthorResponse> authors() {
        final var authors = bookManagementInquiryPort.authors();
        log.debug("authors: {}", authors);
        return GraphqlAuthorResponse.from(authors);
    }

    @QueryMapping
    public GraphqlAuthorResponse authorById(@Argument String id) {
        final var author = bookManagementInquiryPort.authorById(AuthorId.withId(id));
        log.debug("author: {}", author);
        return author.map(GraphqlAuthorResponse::from).orElseThrow(() -> new NotFoundException(id));
    }

    @QueryMapping
    public Collection<GraphqlAuthorResponse> authorsByName(@Argument String name) {
        final var authors = bookManagementInquiryPort.authorsByName(name);
        log.debug("authors: {}", authors);
        return GraphqlAuthorResponse.from(authors);
    }
}

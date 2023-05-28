package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.application.port.in.BookManagementInquiryPort;
import com.putoet.mybooks.books.domain.AuthorId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collection;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GraphqlAuthorController {
    private final BookManagementInquiryPort bookManagementInquiryPort;

    @QueryMapping
    public Collection<GraphqlAuthorResponse> authors() {
        return GraphqlAuthorResponse.from(bookManagementInquiryPort.authors());
    }

    @QueryMapping
    public GraphqlAuthorResponse authorById(@Argument String id) {
        final var author = bookManagementInquiryPort.authorById(AuthorId.withId(id));
        return author.map(GraphqlAuthorResponse::from).orElseThrow(() -> new NotFoundException(id));
    }

    @QueryMapping
    public Collection<GraphqlAuthorResponse> authorsByName(@Argument String name) {
        return GraphqlAuthorResponse.from(bookManagementInquiryPort.authorsByName(name));
    }
}

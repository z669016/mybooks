package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.adapter.in.web.security.UserResponse;
import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GraphqlUserController {
    private final UserManagementPort userManagementPort;

    @QueryMapping
    public Set<UserResponse> users() {
        return UserResponse.from(userManagementPort.users());
    }

    @QueryMapping
    public UserResponse userById(@Argument String id) {
        final var user = userManagementPort.userById(id);
        return user.map(UserResponse::from).orElseThrow(() -> new NotFoundException(id));
    }
}

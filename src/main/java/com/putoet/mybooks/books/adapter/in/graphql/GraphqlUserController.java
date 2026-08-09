package com.putoet.mybooks.books.adapter.in.graphql;

import com.putoet.mybooks.books.adapter.in.web.security.UserResponse;
import com.putoet.mybooks.books.application.port.in.security.UserManagementPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
public class GraphqlUserController {
    public static final Logger log = LoggerFactory.getLogger(GraphqlUserController.class);

    private final UserManagementPort userManagementPort;

    public GraphqlUserController(final UserManagementPort userManagementPort) {
        log.debug("GraphqlUserController('{}')", userManagementPort);
        this.userManagementPort = userManagementPort;
    }

    @QueryMapping
    public Set<UserResponse> users() {
        log.debug("users()");
        final var users = userManagementPort.users();
        log.debug("users returns: {}", users);
        return UserResponse.from(users);
    }

    @QueryMapping
    public UserResponse userById(@Argument String id) {
        log.debug("userById('{}')", id);
        final var user = userManagementPort.userById(id);
        log.debug("user by id returns: {}", user);
        return user.map(UserResponse::from).orElseThrow(() -> new NotFoundException(id));
    }
}

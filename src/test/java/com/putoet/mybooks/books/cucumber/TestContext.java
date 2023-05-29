package com.putoet.mybooks.books.cucumber;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestContext {
    private final static TestContext instance;
    private static final String RESPONSE = "response";
    private static final String TOKEN = "token";
    private static final Set<String> RESERVED_NAMES = Set.of(RESPONSE, TOKEN);

    private final ThreadLocal<Map<String, Object>> testContexts = ThreadLocal.withInitial(HashMap::new);

    static {
        instance = new TestContext();
    }

    private TestContext() {
    }

    public static TestContext getInstance() {
        return instance;
    }

    public Response response() {
        return (Response) testContexts.get().get(RESPONSE);
    }

    public TestContext response(Response response) {
        testContexts.get().put(RESPONSE, response);
        return this;
    }

    public String token() {
        return (String) testContexts.get().get(TOKEN);
    }

    public TestContext token(String token) {
        testContexts.get().put(TOKEN, token);
        return this;
    }

    public <T> T get(String name, Class<T> clazz) {
        if (RESERVED_NAMES.contains(name))
            throw new IllegalArgumentException("Use properly typed methods to retrieve any of " + RESERVED_NAMES);

        return clazz.cast(testContexts.get().get(name));
    }

    public <T> TestContext set(String name, T object) {
        if (RESERVED_NAMES.contains(name))
            throw new IllegalArgumentException("Use properly typed methods to set any of " + RESERVED_NAMES);

        testContexts.get().put(name, object);
        return this;
    }

    public TestContext clear() {
        testContexts.get().clear();
        return this;
    }

}

package com.putoet.mybooks.books.adapter.out.persistence.jdbc;

import org.slf4j.Logger;

public class SqlUtil {
    private SqlUtil() {}

    public static void sqlInfo(Logger log, String sql, Object ... parameters) {
        final var message = sql.replace("?", "'{}'") + ";";
        log.info(message, parameters);
    }
}

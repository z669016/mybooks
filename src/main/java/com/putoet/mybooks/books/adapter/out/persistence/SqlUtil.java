package com.putoet.mybooks.books.adapter.out.persistence;

import org.slf4j.Logger;

public class SqlUtil {
    public static void sqlInfo(Logger log, String sql, Object ... parameters) {
        log.info(sql.replace("?", "'{}'") + ";", parameters);
    }
}

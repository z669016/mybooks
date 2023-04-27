package com.putoet.mybooks.books.adapter.out.persistence;

import org.slf4j.Logger;

public class SqlUtil {
    public static void sqlInfo(Logger logger, String sql, Object ... parameters) {
        logger.info(sql.replace("?", "'{}'") + ";", parameters);
    }
}

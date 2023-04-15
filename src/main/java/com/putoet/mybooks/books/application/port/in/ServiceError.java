package com.putoet.mybooks.books.application.port.in;

/**
 * Enum ServiceError
 * ServiceError is used as possible valid error states which can be returned by a service (In ports), which enables very
 * standardized error handling.
 */
public enum ServiceError {
    AUTHOR_NAME_REQUIRED,
    AUTHOR_ID_REQUIRED,
    AUTHOR_SITE_DETAILS_REQUIRED,
    AUTHOR_SITE_TYPE_REQUIRED,
    AUTHOR_SITE_NOT_SET,
    AUTHOR_SITE_URL_INVALID,
    AUTHOR_NOT_REGISTERED,
    AUTHOR_NOT_UPDATED,
    AUTHOR_DETAILS_REQUIRED,
    AUTHOR_FOR_ID_NOT_FOUND,
    BOOK_ID_REQUIRED,
    BOOK_TITLE_REQUIRED,
    BOOK_AUTHORS_REQUIRED,
    BOOK_FORMAT_REQUIRED,
    BOOK_KEYWORDS_REQUIRED,
    BOOK_NOT_REGISTERED;

    public void raise() {
        throw new ServiceException(this);
    }
    public void raise(String msg) {
        throw new ServiceException(this, msg);
    }
    public void raise(Throwable cause) {
        throw new ServiceException(this, cause);
    }

    public void raise(String msg, Throwable cause) {
        throw new ServiceException(this, msg, cause);
    }
}

package com.putoet.mybooks.application.port.in;

public enum ServiceError {
    AUTHOR_NAME_REQUIRED,
    AUTHOR_ID_REQUIRED,
    AUTHOR_SITE_DETAILS_REQUIRED,
    AUTHOR_SITE_TYPE_REQUIRED,
    AUTHOR_SITE_NOT_SET,
    AUTHOR_SITE_URL_INVALID,
    AUTHOR_NOT_CREATED,
    AUTHOR_DETAILS_REQUIRED,
    AUTHOR_FOR_ID_NOT_FOUND,
    BOOK_TITLE_REQUIRED,
    BOOK_ID_REQUIRED;

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

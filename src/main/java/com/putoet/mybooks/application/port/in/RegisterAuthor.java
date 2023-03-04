package com.putoet.mybooks.application.port.in;

import com.putoet.mybooks.domain.Author;

public interface RegisterAuthor {
    Author registerAuthor(RegisterAuthorCommand command);

    static void error(ServiceError serviceError) {
        throw new RegisterAuthorError(serviceError);
    }

    static void error(ServiceError serviceError, Throwable cause) {
        throw new RegisterAuthorError(serviceError, cause);
    }

    class RegisterAuthorError extends ServiceException {

        public RegisterAuthorError(ServiceError serviceError) {
            super(serviceError);
        }

        public RegisterAuthorError(ServiceError serviceError, Throwable cause) {
            super(serviceError, cause);
        }
    }
}

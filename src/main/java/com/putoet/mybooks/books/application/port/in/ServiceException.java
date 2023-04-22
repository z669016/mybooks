package com.putoet.mybooks.books.application.port.in;

/**
 * Class ServiceException
 * ServiceException enables standardized error handling for services
 */
public class ServiceException extends RuntimeException {
    private final ServiceError serviceError;

    public ServiceException(ServiceError serviceError) {
        super(serviceError.name());
        this.serviceError = serviceError;
    }

    public ServiceException(ServiceError serviceError, String msg) {
        super(serviceError.name() + " - " + msg);
        this.serviceError = serviceError;
    }

    public ServiceError serviceError() {
        return serviceError;
    }
}

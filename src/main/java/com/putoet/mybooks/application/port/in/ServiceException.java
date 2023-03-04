package com.putoet.mybooks.application.port.in;

public class ServiceException extends RuntimeException {
    private final ServiceError serviceError;

    public ServiceException(ServiceError serviceError) {
        this.serviceError = serviceError;
    }

    public ServiceException(ServiceError serviceError, String msg) {
        super(msg);
        this.serviceError = serviceError;
    }

    public ServiceException(ServiceError serviceError, String msg, Throwable cause) {
        super(msg, cause);
        this.serviceError = serviceError;
    }

    public ServiceException(ServiceError serviceError, Throwable cause) {
        super(cause);
        this.serviceError = serviceError;
    }

    public ServiceError serviceError() {
        return serviceError;
    }
}

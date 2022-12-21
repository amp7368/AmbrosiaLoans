package com.ambrosia.loans.database.client;

public class CreateClientException extends Exception {

    public CreateClientException() {
    }

    public CreateClientException(String message) {
        super(message);
    }

    public CreateClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateClientException(Throwable cause) {
        super(cause);
    }

    public CreateClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

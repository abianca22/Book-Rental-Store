package org.booksrental.userservice.exception;

public class NoDataFound extends RuntimeException {
    public NoDataFound(String message) {
        super(message);
    }

    public NoDataFound(String message, Throwable cause) {
        super(message, cause);
    }
}

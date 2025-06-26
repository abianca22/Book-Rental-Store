package org.booksrental.rentalservice.exception;

public class OverdueException extends RuntimeException {
    public OverdueException(String message) {
        super(message);
    }
}

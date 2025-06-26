package org.booksrental.rentalservice.exception;

public class ExistingDataException extends RuntimeException {
    public ExistingDataException(String message) {
        super(message);
    }
}


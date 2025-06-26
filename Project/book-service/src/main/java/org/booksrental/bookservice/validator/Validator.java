package org.booksrental.bookservice.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import java.util.Set;

public class Validator {
    private static jakarta.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static void validateObject(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new RuntimeException(violations.stream().map(ConstraintViolation::getMessage).reduce(" ", (previousStrings, nextString) -> previousStrings + nextString));
        }
    }

}
package com.example.books_rental.validator;

import com.example.books_rental.dto.UpdateCategoryDto;
import com.example.books_rental.dto.UpdateUserDto;
import com.example.books_rental.model.entities.Category;
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

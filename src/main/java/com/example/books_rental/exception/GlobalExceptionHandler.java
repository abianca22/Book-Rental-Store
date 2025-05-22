package com.example.books_rental.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handle(Exception exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "layout";
    }

    @ExceptionHandler(InvalidDataException.class)
    public String handle(InvalidDataException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage() + " " + exception.getCause().getMessage());
        return "layout";
    }
}

//package com.example.books_rental.exception;
//
//import io.swagger.v3.oas.annotations.Hidden;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.io.IOException;
//
//@Hidden
//@RestControllerAdvice
//public class ExceptionHandlerAdvice {
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handle(Exception exception) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
//    }
//
//    @ExceptionHandler(InvalidDataException.class)
//    public ResponseEntity<String> handle(InvalidDataException exception) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage() + " " + exception.getCause().getMessage());
//    }
//}


package com.example.books_rental.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(Exception.class)
    public String handle(Exception exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "exceptionPage";
    }

    @ExceptionHandler(InvalidDataException.class)
    public String handle(InvalidDataException exception, Model model) {
        model.addAttribute("errorMessage", exception.getMessage() + " " + exception.getCause().getMessage());
        return "exceptionPage";
    }
}

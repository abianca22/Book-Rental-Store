package com.example.books_rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BooksRentalApplication {

    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(BooksRentalApplication.class, args);

    }

}

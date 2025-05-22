package com.example.books_rental.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class CreateBookDto {
    private String title;
    private String author;
    private String description;
    private String publishingHouse;
    private List<Integer> categoryIds = new ArrayList<>();
    private boolean status = true;
    private float rentalPrice;
    private float extensionPrice;
}

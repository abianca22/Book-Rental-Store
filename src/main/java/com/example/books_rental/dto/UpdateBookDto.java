package com.example.books_rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Setter
@Getter
@ToString
public class UpdateBookDto {
    private Integer id;
    @NotBlank(message = "Title is mandatory")
    private String title;
    @NotBlank(message = "Author is mandatory")
    private String author;
    private String description;
    @NotBlank(message = "Publishing house is mandatory")
    private String publishingHouse;
    private List<Integer> categoryIds = new ArrayList<>();
    private boolean status = true;
    @Positive(message = "Rental price must be positive")
    private float rentalPrice;
    @PositiveOrZero(message = "Extension price must have a numeric value")
    private float extensionPrice;

    public UpdateBookDto(Integer id) {
        this.id = id;
    }
}

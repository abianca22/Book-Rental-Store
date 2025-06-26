package org.booksrental.bookservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CreateBookDTO {
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
}

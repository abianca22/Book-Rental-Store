package org.booksrental.bookservice.model.dto;

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
public class UpdateBookDTO {
    @NotNull
    private Integer id;
    private String title;
    private String author;
    private String description;
    private String publishingHouse;
    private List<Integer> categoryIds = new ArrayList<>();
    private boolean status = true;
    @Positive(message = "Rental price must be positive")
    private float rentalPrice;
    private float extensionPrice;
}

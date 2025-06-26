package com.example.books_rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryDto {
    @NotNull
    private Integer id;
    private String name;
    private String description;

    public UpdateCategoryDto(Integer id) {
        this.id = id;
    }
}

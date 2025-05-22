package com.example.books_rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class UpdateRentalDto {
    @NotNull
    private Integer id;
    private LocalDate dueDate;
    private Integer bookId;
    private Integer clientId;
    private Integer employeeId;

    public UpdateRentalDto(Integer id) {
        this.id = id;
    }
}

package com.example.books_rental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnDto {
    @NotNull
    private int rentalId;
    @NotNull
    private int employeeId;
}

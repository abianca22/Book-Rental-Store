package com.example.books_rental.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateUserDto {
    private String username;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{7,}$", message = "Password must have at least one uppercase letter, one lowercase letter, one digit and one special character")
    @Size(max = 25, message = "Password must have at most 25 characters")
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
}

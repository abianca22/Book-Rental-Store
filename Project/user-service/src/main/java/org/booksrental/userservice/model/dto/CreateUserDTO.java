package org.booksrental.userservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateUserDTO extends ViewUserDTO {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{7,}$", message = "Password must have at least one uppercase letter, one lowercase letter, one digit and one special character")
    @Size(max = 25, message = "Password must have at most 25 characters")
    private String password;
    @NotBlank
    @Size(max = 25, min = 5, message = "Username must have between 5 and 25 characters")
    private String username;
    @NotBlank
    @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must have 10 digits and start with 07")
    private String phone;
    @NotBlank
    @Email
    private String email;
}

package org.booksrental.userservice.model.dto;

import jakarta.validation.constraints.Email;
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
public class ViewUserDTO {
    private Integer id;
    @Size(max = 25, min = 5, message = "Username must have between 5 and 25 characters")
    private String username;
    private String firstname;
    private String lastname;
    @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must have 10 digits and start with 07")
    private String phone;
    @Email(message = "Email should be valid")
    private String email;
    private String role;
}

package org.booksrental.userservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class UpdateUserDTO extends ViewUserDTO {
    @NotNull
    private Integer id;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{7,}$", message = "Password must have at least one uppercase letter, one lowercase letter, one digit and one special character")
    @Size(max = 25, message = "Password must have at most 25 characters")
    private String password;
}

package org.booksrental.rentalservice.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private Integer id;
    private String username;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private String role;
}

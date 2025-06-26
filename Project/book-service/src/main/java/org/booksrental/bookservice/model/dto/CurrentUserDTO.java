package org.booksrental.bookservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
public class CurrentUserDTO {
    private String username;
    private String role;
    private Integer id;
}

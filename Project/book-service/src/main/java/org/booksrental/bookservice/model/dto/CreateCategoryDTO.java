package org.booksrental.bookservice.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateCategoryDTO {
    private String name;
    private String description;
}

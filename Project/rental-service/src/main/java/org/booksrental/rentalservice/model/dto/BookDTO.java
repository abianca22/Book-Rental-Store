package org.booksrental.rentalservice.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Setter
@Getter
@ToString
public class BookDTO {
    private Integer id;
    private String title;
    private boolean status = true;
    private float rentalPrice;
    private float extensionPrice;
}

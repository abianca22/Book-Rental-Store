package org.booksrental.rentalservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateRentalDTO {
    private Integer id;
    private LocalDate dueDate;
    private Integer bookId;
    private Integer clientId;
    private Integer employeeId;
    private Integer associatedReturnId;
    private boolean pendingRequest = false;
    private float totalPrice;

}
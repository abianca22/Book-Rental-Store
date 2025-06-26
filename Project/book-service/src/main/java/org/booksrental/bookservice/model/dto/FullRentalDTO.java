package org.booksrental.bookservice.model.dto;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FullRentalDTO {
    private Integer id;
    private LocalDate dueDate;
    private Integer bookId;
    private Integer clientId;
    private Integer employeeId;
    private Integer associatedReturnId;
    private boolean pendingRequest = false;
    private float totalPrice;

}

package org.booksrental.rentalservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnDTO {
    @NotNull
    private Integer rentalId;
    private Integer employeeId;
}

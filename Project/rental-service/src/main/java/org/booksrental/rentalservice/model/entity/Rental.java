package org.booksrental.rentalservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "rentals")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString()
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "book_id")
    private Integer bookId;
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;
    @Column(name = "employee_id")
    private Integer employeeId;
    @OneToOne(mappedBy = "rental")
    private Return associatedReturn;
    @Positive
    private float totalPrice;
    private boolean pendingRequest = false;
}

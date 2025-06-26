package org.booksrental.rentalservice.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "returns")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Return {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Temporal(TemporalType.DATE)
    private LocalDate returnDate;
    @Column(name = "employee_id")
    private Integer employeeId;
    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "rental_id")
    private Rental rental;
}

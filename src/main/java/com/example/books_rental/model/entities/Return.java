package com.example.books_rental.model.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

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
    private int id;
    @OneToOne
    @JoinColumn(name = "rental_id")
    private Rental rental;
    @Temporal(TemporalType.DATE)
    private LocalDate returnDate;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonManagedReference
    private User employee;
}

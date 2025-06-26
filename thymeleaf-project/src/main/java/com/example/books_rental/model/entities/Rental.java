package com.example.books_rental.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jdk.jfr.BooleanFlag;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "rentals")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"client", "book", "employee", "associatedReturn"})
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonManagedReference
    private User client;
    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonManagedReference
    private Book book;
    @Temporal(TemporalType.DATE)
    private LocalDate startDate;
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonManagedReference
    private User employee;
    @OneToOne(mappedBy = "rental")
    @JsonIgnore
    private Return associatedReturn;
    @Positive
    private float totalPrice;
    private boolean pendingRequest = false;

}

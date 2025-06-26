package com.example.books_rental.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Username is mandatory")
    @Size(max = 25, min = 5, message = "Username must have between 5 and 25 characters")
    private String username;

    @NotBlank
    private String password;

    private String firstname;

    private String lastname;

    @NotBlank(message = "Phone is mandatory")
    @Pattern(regexp = "^07[0-9]{8}$", message = "Phone number must have 10 digits and start with 07")
    private String phone;

    @NotBlank(message = "Email is mandatory")
    @Email
    private String email;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @JsonManagedReference
    private Role role;

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Rental> employeeRentals;

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Rental> clientRentals;

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Return> returns;

    @Builder.Default
    private Boolean accountNonExpired = true;

    @Builder.Default
    private Boolean accountNonLocked = true;

    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @Builder.Default
    private Boolean enabled = true;

}

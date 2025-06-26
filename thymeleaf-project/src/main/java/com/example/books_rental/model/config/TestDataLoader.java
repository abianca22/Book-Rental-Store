package com.example.books_rental.model.config;

import com.example.books_rental.model.entities.*;
import com.example.books_rental.repository.*;
import com.example.books_rental.service.AuthenticationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final ReturnRepository returnRepository;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    public TestDataLoader(BookRepository bookRepository,
                          CategoryRepository categoryRepository,
                          RoleRepository roleRepository,
                          UserRepository userRepository,
                          RentalRepository rentalRepository,
                          ReturnRepository returnRepository,
                          AuthenticationService authenticationService,
                          PasswordEncoder passwordEncoder) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
        this.returnRepository = returnRepository;
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Category fantasy = new Category();
        fantasy.setName("Fantasy");
        categoryRepository.save(fantasy);
        Category horror = new Category();
        horror.setName("Horror");
        horror.setDescription("Books that are intended to scare the reader.");
        categoryRepository.save(horror);

        Book book = Book.builder()
                .title("Harry Potter și Piatra Filozofală")
                .author("J.K. Rowling")
                .publishingHouse("Bloomsbury")
                .rentalPrice(12.5f)
                .extensionPrice(2.5f)
                .description("Prima carte din seria Harry Potter.")
                .status(false)
                .categories(List.of(fantasy))
                .build();

        bookRepository.save(book);
        Book book2 = Book.builder()
                .title("Harry Potter și Camera Secretelor")
                .author("J.K. Rowling")
                .publishingHouse("Bloomsbury")
                .rentalPrice(12.5f)
                .extensionPrice(2.5f)
                .description("A doua carte din seria Harry Potter.")
                .status(true)
                .categories(List.of(fantasy))
                .build();
        bookRepository.save(book2);
        Book book3 = Book.builder()
                .title("Harry Potter și Camera Secretelor")
                .author("J.K. Rowling")
                .publishingHouse("Bloomsbury")
                .rentalPrice(12.5f)
                .extensionPrice(2.5f)
                .description("A doua carte din seria Harry Potter.")
                .status(false)
                .categories(List.of(fantasy))
                .build();
        bookRepository.save(book3);
        Role adminRole = Role.builder().name("admin").build();
        Role employeeRole = Role.builder().name("employee").build();
        Role clientRole = Role.builder().name("client").build();

        roleRepository.saveAll(List.of(adminRole, employeeRole, clientRole));

        User admin = User.builder().username("admin").password(passwordEncoder.encode("Admin1@")).email("admin@email.com").phone("0700000000").role(adminRole).build();
        userRepository.save(admin);

        User employee = User.builder().username("employee").password(passwordEncoder.encode("Employee1@")).email("employee@email.com").phone("0700000001").role(employeeRole).build();
        userRepository.save(employee);

        User client = User.builder().username("client").password(passwordEncoder.encode("Client1@")).email("client@email.com").phone("0700000002").role(clientRole).build();
        userRepository.save(client);

        Rental rental = Rental.builder()
                .client(client)
                .book(book)
                .employee(employee)
                .totalPrice(book.getRentalPrice())
                .startDate(java.time.LocalDate.now())
                .dueDate(java.time.LocalDate.now().plusDays(14))
                .pendingRequest(false)
                .build();
        book.setStatus(true);
        bookRepository.save(book);
        rentalRepository.save(rental);

        Rental rental2 = Rental.builder()
                .client(client)
                .book(book2)
                .employee(employee)
                .totalPrice(book2.getRentalPrice())
                .startDate(java.time.LocalDate.now().minusDays(11))
                .dueDate(java.time.LocalDate.now().plusDays(3))
                .pendingRequest(false)
                .build();
        book2.setStatus(true);
        bookRepository.save(book2);
        rentalRepository.save(rental2);

        Rental rental3 = Rental.builder()
                .client(client)
                .book(book3)
                .employee(employee)
                .totalPrice(book3.getRentalPrice())
                .startDate(java.time.LocalDate.now().minusDays(20))
                .dueDate(java.time.LocalDate.now().minusDays(6))
                .pendingRequest(false)
                .build();
        rentalRepository.save(rental3);

        Return return1 = Return.builder()
                .rental(rental3)
                .employee(admin)
                .returnDate(java.time.LocalDate.now().minusDays(7))
                .build();
        returnRepository.save(return1);



    }
}

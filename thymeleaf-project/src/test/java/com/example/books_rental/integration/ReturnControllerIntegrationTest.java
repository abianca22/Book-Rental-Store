package com.example.books_rental.integration;

import com.example.books_rental.TestDbUtils;
import com.example.books_rental.model.entities.*;
import com.example.books_rental.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReturnControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestDbUtils testDbUtils;

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private ReturnRepository returnRepository;

    private User admin;
    private User employee;
    private User client;
    private Book book;
    private Rental rental;

    @BeforeEach
    void setUp() {
        testDbUtils.disableReferentialIntegrity();
        returnRepository.deleteAll();
        rentalRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        testDbUtils.enableReferentialIntegrity();

        Role adminRole = roleRepository.save(Role.builder().name("admin").build());
        Role employeeRole = roleRepository.save(Role.builder().name("employee").build());
        Role clientRole = roleRepository.save(Role.builder().name("client").build());

        admin = userRepository.save(User.builder()
                .username("admin").email("admin@example.com")
                .password("Password1@").phone("0700000001").role(adminRole).build());

        employee = userRepository.save(User.builder()
                .username("employee").email("employee@example.com")
                .password("Password1@").phone("0700000002").role(employeeRole).build());

        client = userRepository.save(User.builder()
                .username("client").email("client@example.com")
                .password("Password1@").phone("0700000003").role(clientRole).build());

        book = bookRepository.save(Book.builder()
                .title("Test Book").author("Test Author")
                .publishingHouse("Test PH")
                .rentalPrice(10).extensionPrice(2)
                .status(false) // already rented
                .build());

        rental = rentalRepository.save(Rental.builder()
                .book(book).client(client).employee(employee)
                .startDate(LocalDate.now().minusDays(3))
                .dueDate(LocalDate.now().plusDays(4))
                .totalPrice(book.getRentalPrice())
                .pendingRequest(true)
                .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void addReturn_AsAdmin_Success() throws Exception {
        mockMvc.perform(post("/returns/" + rental.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"));

        Return savedReturn = returnRepository.findAll().get(0);
        assert savedReturn.getRental().getId() == rental.getId();
        assert savedReturn.getReturnDate() != null;
        assert savedReturn.getEmployee().getUsername().equals("admin");

        // Book should be available again
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assert updatedBook.isStatus();

        // Rental should no longer be pending
        Rental updatedRental = rentalRepository.findById(rental.getId()).orElseThrow();
        assert !updatedRental.isPendingRequest();
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void addReturn_AsClient_ShouldFail() throws Exception {
        mockMvc.perform(post("/returns/" + rental.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("error"));

        assert returnRepository.findAll().isEmpty();
    }


    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void addReturn_WhenReturnAlreadyExists_ShouldFail() throws Exception {
        Return existingReturn = Return.builder()
                .rental(rental).employee(admin)
                .returnDate(LocalDate.now())
                .build();
        returnRepository.save(existingReturn);

        mockMvc.perform(post("/returns/" + rental.getId()).with(csrf()))
                .andExpect(view().name("error"));
    }
}

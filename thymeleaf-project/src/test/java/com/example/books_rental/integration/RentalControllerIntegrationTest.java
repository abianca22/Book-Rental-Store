package com.example.books_rental.integration;

import com.example.books_rental.TestDbUtils;
import com.example.books_rental.model.entities.*;
import com.example.books_rental.repository.*;
import com.example.books_rental.service.RentalsManagementService;
import com.example.books_rental.service.ReturnsManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RentalControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestDbUtils testDbUtils;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private ReturnRepository returnRepository;

    private User admin;
    private User client;
    private Role adminRole;
    private Role clientRole;
    private Book book;
    private Rental rental;
    @Autowired
    private RentalsManagementService rentalsManagementService;
    @Autowired
    private ReturnsManagementService returnsManagementService;

    @BeforeEach
    void setup() {
        testDbUtils.disableReferentialIntegrity();
        rentalRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        testDbUtils.enableReferentialIntegrity();

        adminRole = roleRepository.save(Role.builder().name("admin").build());
        clientRole = roleRepository.save(Role.builder().name("client").build());

        admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Password1@")
                .phone("0700000001")
                .role(adminRole)
                .build());

        client = userRepository.save(User.builder()
                .username("client")
                .email("client@example.com")
                .password("Password1@")
                .phone("0700000002")
                .role(clientRole)
                .build());

        book = bookRepository.save(Book.builder()
                .title("Book")
                .author("Author")
                .publishingHouse("House")
                .rentalPrice(10)
                .extensionPrice(2)
                .status(true)
                .build());

        rental = rentalRepository.save(Rental.builder()
                .book(book)
                .client(client)
                .employee(admin)
                .startDate(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(5))
                        .totalPrice(book.getRentalPrice())
                .pendingRequest(false)
                .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void getRentals_AsAdmin_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals/rentalsList"))
                .andExpect(model().attributeExists("rentals"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void getRentals_AsClient_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals/rentalsList"))
                .andExpect(model().attributeExists("rentals"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void requestExtension_AsClient_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/rentals/editClient/" + rental.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void approveExtension_AsAdmin_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/rentals/edit/" + rental.getId())
                        .param("id", String.valueOf(rental.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void editRental_AsOtherClient_ShouldFail() throws Exception {
        User otherClient = userRepository.save(User.builder()
                .username("other")
                .email("other@example.com")
                .password("Password1@")
                .phone("0700000003")
                .role(clientRole)
                .build());

        mockMvc.perform(post("/rentals/editClient/" + rental.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rentals"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void editRental_WithInvalidEmail_ShouldShowError() throws Exception {
        mockMvc.perform(post("/rentals/edit/" + rental.getId())
                        .param("email", "nonexistent@example.com")
                        .param("id", String.valueOf(rental.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("rentals/rentalEditForm"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void getRental_WithReturn_ShouldDenyAccess() throws Exception {
        Return returnEntity = returnRepository.save(Return.builder()
                .returnDate(LocalDate.now())
                .build());
        Rental returnedRental = rentalRepository.save(Rental.builder()
                .book(book)
                .client(client)
                .employee(admin)
                .startDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(5))
                .pendingRequest(false)
                .totalPrice(book.getRentalPrice())
                .build());
        returnsManagementService.addReturn(returnEntity, returnedRental, admin);
            mockMvc.perform(get("/rentals/edit/" + returnedRental.getId()))
                    .andExpect(view().name("access_denied"));

    }
}

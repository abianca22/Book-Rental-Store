package com.example.books_rental.integration;

import com.example.books_rental.TestDbUtils;
import com.example.books_rental.model.entities.Book;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.BookRepository;
import com.example.books_rental.repository.CategoryRepository;
import com.example.books_rental.repository.RoleRepository;
import com.example.books_rental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestDbUtils testDbUtils;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BookRepository bookRepository;

    private Category testCategory;

    @BeforeEach
    @Transactional
    void setup() {
        testDbUtils.disableReferentialIntegrity();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        testDbUtils.enableReferentialIntegrity();

        Role adminRole = roleRepository.save(Role.builder().name("admin").build());
        Role clientRole = roleRepository.save(Role.builder().name("client").build());

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Password1@")
                .phone("0700000001")
                .role(adminRole)
                .build());

        userRepository.save(User.builder()
                .username("client")
                .email("client@example.com")
                .password("Password1@")
                .phone("0700000002")
                .role(clientRole)
                .build());

        testCategory = categoryRepository.save(Category.builder()
                .name("Drama")
                .description("Dramatic books")
                .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void addBook_AsAdmin_ShouldSucceed() throws Exception {
        MockMultipartFile image = new MockMultipartFile("imagefile", "cover.jpg", "image/jpeg", "test-image".getBytes());

        mockMvc.perform(multipart("/books/form")
                        .file(image)
                        .with(csrf())
                        .param("title", "Test Book")
                        .param("author", "Test Author")
                        .param("publishingHouse", "Test House")
                        .param("rentalPrice", "10.0")
                        .param("extensionPrice", "2.0")
                        .param("description", "Test Description")
                        .param("categoryId", String.valueOf(testCategory.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void addBook_AsClient_ShouldFail() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", "test-image".getBytes());

        mockMvc.perform(multipart("/books/form")
                        .file(image)
                        .with(csrf())
                        .param("title", "Client Book")
                        .param("author", "Client Author")
                        .param("publishingHouse", "Client House")
                        .param("rentalPrice", "5.0")
                        .param("extensionPrice", "1.0")
                        .param("description", "Client Description")
                        .param("categoryId", String.valueOf(testCategory.getId())))
                .andExpect(forwardedUrl("/accessDenied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void listBooks_ShouldShowList() throws Exception {
        bookRepository.save(Book.builder()
                .title("Drama Book")
                .author("Author")
                .publishingHouse("House")
                .rentalPrice(10)
                .extensionPrice(2)
                .description("Description")
                .status(true)
                .categories(List.of(testCategory))
                .build());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/pageableBooks"))
                .andExpect(model().attributeExists("bookPage"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void deleteBook_AsAdmin_ShouldSucceed() throws Exception {
        Book book = bookRepository.save(Book.builder()
                .title("Delete Me")
                .author("Author")
                .publishingHouse("House")
                .rentalPrice(10)
                .extensionPrice(2)
                .status(true)
                .categories(List.of(testCategory))
                .build());

        mockMvc.perform(post("/books/delete/" + book.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void deleteBook_AsClient_ShouldFail() throws Exception {
        Book book = bookRepository.save(Book.builder()
                .title("Protected")
                .author("Author")
                .publishingHouse("House")
                .rentalPrice(10)
                .extensionPrice(2)
                .status(true)
                .categories(List.of(testCategory))
                .build());

        mockMvc.perform(post("/books/delete/" + book.getId()).with(csrf()))
                .andExpect(forwardedUrl("/accessDenied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void viewBookDetails_ShouldWork() throws Exception {
        Book book = bookRepository.save(Book.builder()
                .title("Details Book")
                .author("Author")
                .publishingHouse("House")
                .rentalPrice(10)
                .extensionPrice(2)
                .description("Detailed desc")
                .status(true)
                .categories(List.of(testCategory))
                .build());

        mockMvc.perform(get("/books/" + book.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("books/bookDetails"))
                .andExpect(model().attributeExists("book"));
    }
}

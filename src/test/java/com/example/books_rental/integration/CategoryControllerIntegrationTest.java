package com.example.books_rental.integration;

import com.example.books_rental.TestDbUtils;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.CategoryRepository;
import com.example.books_rental.repository.RoleRepository;
import com.example.books_rental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TestDbUtils testDbUtils;

    @BeforeEach
    @Transactional
    void setup() {
        testDbUtils.disableReferentialIntegrity();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        testDbUtils.enableReferentialIntegrity();

        Role staffRole = roleRepository.save(Role.builder().name("admin").build());
        Role clientRole = roleRepository.save(Role.builder().name("client").build());

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Password1@")
                .phone("0700000002")
                .role(staffRole)
                .build());

        userRepository.save(User.builder()
                .username("client")
                .email("client@example.com")
                .password("Password1@")
                .phone("0700000003")
                .role(clientRole)
                .build());
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void addCategory_AsAdmin_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Fantasy")
                        .param("description", "Fantasy books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void addCategory_AsClient_ShouldFail() throws Exception {
        mockMvc.perform(post("/categories/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Sci-Fi")
                        .param("description", "Science fiction books"))
                .andExpect(forwardedUrl("/accessDenied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void listCategories_ShouldReturnListPage() throws Exception {
        categoryRepository.save(Category.builder().name("Adventure").description("Adventure books").build());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("authenticated"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void deleteCategory_AsAdmin_ShouldRedirect() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("To Delete").description("To Delete").build());

        mockMvc.perform(post("/categories/delete/" + category.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));
    }

    @Test
    @WithMockUser(username = "client", roles = "client")
    void viewBooksByCategory_ShouldRedirectToBooks() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Adventure").description("Desc").build());

        mockMvc.perform(get("/categories/" + category.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books?categoryId=" + category.getId()));
    }
}

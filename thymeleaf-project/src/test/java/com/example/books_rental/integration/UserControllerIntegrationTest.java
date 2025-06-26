package com.example.books_rental.integration;

import com.example.books_rental.TestDbUtils;
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
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestDbUtils testDbUtils;


    private User testUser;
    private Role clientRole;
    private Role adminRole;


    @BeforeEach
    @Transactional
    void setUp() {
        testDbUtils.disableReferentialIntegrity();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        categoryRepository.deleteAll();
        bookRepository.deleteAll();
        testDbUtils.enableReferentialIntegrity();

        clientRole = roleRepository.save(Role.builder().name("client").build());
        adminRole = roleRepository.save(Role.builder().name("admin").build());

        testUser = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password1@")
                .phone("0700000001")
                .role(clientRole)
                .build());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "client")
    void profilePage_ShouldReturnUserProfile() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(view().name("users/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "client")
    void editOwnProfile_ShouldReturnForm() throws Exception {
        mockMvc.perform(get("/users/edit/" + testUser.getId())
                        )
                .andExpect(view().name("users/editDataForm"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    void accessUserList_AsAdmin_ShouldWork() throws Exception {
        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Admin@123")
                .phone("0700000002")
                .role(adminRole)
                .build());

        mockMvc.perform(get("/users")
                        .with(csrf()))
                .andExpect(view().name("users/usersList"))
                .andExpect(model().attributeExists("userPage"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "client")
    void accessUserList_AsClient_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(forwardedUrl("/accessDenied"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "client")
    void deleteOwnAccount_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/users/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?accountDeleted"));
    }

    @Test
    void registerNewUser_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("phone", "0700000099")
                        .param("password", "Password1@")
                        .param("confirmPassword", "Password1@"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void registerUser_WithTakenEmail_ShouldReturnError() throws Exception {
        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("Admin@123")
                .phone("0700000002")
                .role(adminRole)
                .build());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("email", "admin@example.com")
                        .param("phone", "0700000011")
                        .param("password", "Password1@")
                        .param("confirmPassword", "Password1@"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }
}

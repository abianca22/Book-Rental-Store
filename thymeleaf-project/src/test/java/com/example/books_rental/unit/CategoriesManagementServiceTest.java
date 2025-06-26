package com.example.books_rental.unit;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.ExistingDataException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Category;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.CategoryRepository;
import com.example.books_rental.repository.RoleRepository;
import com.example.books_rental.repository.UserRepository;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.CategoriesManagementService;
import com.example.books_rental.service.UsersManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class CategoriesManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private CategoriesManagementService categoriesManagementService;


    private User user;
    private User admin;
    private Role clientRole;
    private Role adminRole;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        clientRole = Role.builder().id(1).name("client").build();
        adminRole = Role.builder().id(2).name("admin").build();

        user = User.builder()
                .id(1)
                .username("testuser")
                .email("testuser@email.com")
                .password("Test123@")
                .phone("0700000000")
                .role(clientRole)
                .build();

        admin = User.builder()
                .id(2)
                .username("adminuser")
                .email("adminuser@email.com")
                .password("Admin123@")
                .phone("0700000001")
                .role(adminRole)
                .build();

        category = Category.builder()
                .id(1)
                .name("Fiction")
                .description("Fictional books")
                .build();
    }

    @Test
    void getCategoryById_CategoryExists_ReturnsCategory() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        Category result = categoriesManagementService.getById(1);
        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsException() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> categoriesManagementService.getById(1));
    }

    @Test
    void createCategory_Success() {
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoriesManagementService.addCategory(category);
        assertNotNull(result);
        assertEquals("Fiction", result.getName());
    }

    @Test
    void createCategory_NameExists_ThrowsException() {
        Category newCategory = Category.builder()
                .name("Fiction")
                .description("Fictional books")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.of(category));
        assertThrows(ExistingDataException.class, () -> categoriesManagementService.addCategory(newCategory));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_AccessDenied_ThrowsException() {
        Category newCategory = Category.builder()
                .name("Fiction")
                .description("Fictional books")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByName("Fiction")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> categoriesManagementService.addCategory(newCategory));
        verify(categoryRepository, never()).save(any(Category.class));
    }



    @Test
    void updateCategory_Success() {
        Category updatedCategory = Category.builder()
                .id(1)
                .name("Non-Fiction")
                .description("Non-Fictional books")
                .build();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        when(categoryRepository.findByName("Non-Fiction")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        Category result = categoriesManagementService.updateCategory(updatedCategory);
        assertEquals("Non-Fiction", result.getName());
        assertEquals("Non-Fictional books", result.getDescription());
    }

    @Test
    void updateCategory_AccessDenied_ThrowsException() {
        Category categoryToUpdate = Category.builder()
                .id(1)
                .name("Non-Fiction")
                .description("Non-Fictional books")
                .build();
        when(authenticationService.getCurrentUser()).thenReturn(user);
        assertThrows(AccessDeniedException.class, () -> categoriesManagementService.updateCategory(categoryToUpdate));
    }


    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authenticationService.getCurrentUser()).thenReturn(admin);

        categoriesManagementService.deleteCategory(1);
        verify(categoryRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteCategory_AccessDenied_ThrowsAccessDenied() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(authenticationService.getCurrentUser()).thenReturn(user);

        assertThrows(AccessDeniedException.class, () -> categoriesManagementService.deleteCategory(1));
        verify(categoryRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(admin);
        assertThrows(NotFoundException.class, () -> categoriesManagementService.deleteCategory(1));
    }
}

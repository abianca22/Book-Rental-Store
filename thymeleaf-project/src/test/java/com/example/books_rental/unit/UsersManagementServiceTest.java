package com.example.books_rental.unit;

import com.example.books_rental.exception.AccessDeniedException;
import com.example.books_rental.exception.ExistingDataException;
import com.example.books_rental.exception.NotFoundException;
import com.example.books_rental.model.entities.Role;
import com.example.books_rental.model.entities.User;
import com.example.books_rental.repository.RoleRepository;
import com.example.books_rental.repository.UserRepository;
import com.example.books_rental.service.AuthenticationService;
import com.example.books_rental.service.UsersManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UsersManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UsersManagementService usersManagementService;


    private User user;
    private Role clientRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        clientRole = Role.builder().id(1).name("client").build();

        user = User.builder()
                .id(1)
                .username("testuser")
                .email("testuser@email.com")
                .password("Test123@")
                .phone("0700000000")
                .role(clientRole)
                .build();
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = usersManagementService.getUserById(1);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> usersManagementService.getUserById(1));
    }

    @Test
    void createUser_Success() {
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser@email.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("0700000000")).thenReturn(Optional.empty());
        when(roleRepository.findByName("client")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = usersManagementService.createUser(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Simulează un user NOU (fără ID)
        User newUser = User.builder()
                .username("testuser")
                .email("testuser@email.com")
                .password("Test123@")
                .phone("0700000000")
                .role(clientRole)
                .build();

        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(roleRepository.findByName("client")).thenReturn(Optional.of(clientRole));

        assertThrows(ExistingDataException.class, () -> usersManagementService.createUser(newUser));

        // Verifică că userRepository.save() NU a fost apelat
        verify(userRepository, never()).save(any(User.class));
    }



    @Test
    void updateUser_Success() {
        User updatedUser = User.builder()
                .id(1)
                .username("newuser")
                .email("newemail@email.com")
                .password("NewPass1@")
                .phone("0700000001")
                .role(clientRole)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findUserByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newemail@email.com")).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = usersManagementService.updateUser(updatedUser);

        assertEquals("newuser", result.getUsername());
        assertEquals("newemail@email.com", result.getEmail());
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        User userToUpdate = User.builder()
                .id(123) // id inexistent
                .username("newUsername")
                .build();

        // Simulează un utilizator "client" logat
        User requesterUser = User.builder()
                .id(123)
                .role(Role.builder().name("client").build())
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(requesterUser);

        assertThrows(NotFoundException.class, () -> usersManagementService.updateUser(userToUpdate));
    }


    @Test
    void updateUser_ClientTriesToUpdateAnotherUser_ThrowsAccessDenied() {
        User anotherUser = User.builder().id(2).username("other").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(authenticationService.getCurrentUser()).thenReturn(anotherUser);
        anotherUser.setRole(clientRole);

        assertThrows(AccessDeniedException.class, () -> usersManagementService.updateUser(user));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(authenticationService.getCurrentUser()).thenReturn(user);

        usersManagementService.deleteUser(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteUser_Unauthorized_ThrowsAccessDenied() {
        User anotherUser = User.builder().id(2).role(clientRole).build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(authenticationService.getCurrentUser()).thenReturn(anotherUser);

        assertThrows(AccessDeniedException.class, () -> usersManagementService.deleteUser(1));
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(user);

        assertThrows(NotFoundException.class, () -> usersManagementService.deleteUser(1));
    }
}
